/*
 * Copyright 2011-2021 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.sbt

import java.io.File

import _root_.io.gatling.sbt.utils.CopyUtils._
import _root_.io.gatling.sbt.utils.DependenciesAnalysisResult
import _root_.io.gatling.sbt.utils.DependenciesAnalyzer
import _root_.io.gatling.sbt.utils.FatJar
import _root_.io.gatling.sbt.utils.ReportUtils._
import _root_.io.gatling.sbt.utils.StartRecorderUtils._

import sbt._
import sbt.Keys._

object GatlingTasks {

  private val LeadingSpacesRegex = """^(\s+)"""

  private def forkOptionsWithRunJVMOptions(options: Seq[String]) =
    _root_.sbt.ForkOptions().withRunJVMOptions(options.toVector)

  private def moduleDescriptorConfig = Def.task {
    moduleSettings.value match {
      case config: ModuleDescriptorConfiguration => config
      case x =>
        throw new IllegalStateException(s"sbt-frontline expected a ModuleDescriptorConfiguration, but got a ${x.getClass}")
    }
  }

  def assemblyFrontLine(config: Configuration): Def.Initialize[Task[File]] = Def.task {
    val moduleDescriptor = moduleDescriptorConfig.value

    val DependenciesAnalysisResult(gatlingVersion, dependencies) = DependenciesAnalyzer.analyze(
      dependencyResolution.value,
      updateConfiguration.value,
      (unresolvedWarningConfiguration in update).value,
      config,
      streams.value.log,
      moduleDescriptor
    )

    val classesDirectories = (fullClasspath in config).value.map(_.data).filter(_.isDirectory)

    val jarName = s"${moduleName.value}-frontline-${version.value}"

    FatJar
      .packageFatJar(moduleDescriptor.module, classesDirectories, gatlingVersion, dependencies, target.value, jarName)
  }

  def recorderRunner(config: Configuration, parent: Configuration): Def.Initialize[InputTask[Int]] = Def.inputTask {
    // Parse args and add missing args if necessary
    val args = optionsParser.parsed
    val simulationsForlderArg = toShortOptionAndValue("sf" -> (config / scalaSource).value.getPath)
    val resourcesFolderArg = toShortOptionAndValue("rf" -> (config / resourceDirectory).value.getPath)
    val allArgs = addPackageIfNecessary(args ++ simulationsForlderArg ++ resourcesFolderArg, organization.value)

    val fork = new Fork("java", Some("io.gatling.recorder.GatlingRecorder"))
    val classpathElements = (dependencyClasspath in parent).value.map(_.data) :+ (config / resourceDirectory).value
    val classpath = buildClassPathArgument(classpathElements)
    fork(forkOptionsWithRunJVMOptions(classpath), allArgs)
  }

  def cleanReports(folder: File): Unit = IO.delete(folder)

  def openLastReport(config: Configuration): Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val selectedSimulationId = stateBasedParser(config / target)(target => simulationIdParser(allSimulationIds(target))).parsed
    val filteredReports = filterReportsIfSimulationIdSelected(allReports((config / target).value), selectedSimulationId)
    val reportsPaths = filteredReports.map(_.path)
    reportsPaths.headOption.foreach(file => openInBrowser((file / "index.html").toURI))
  }

  def generateGatlingReport(config: Configuration): Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val selectedReportName = stateBasedParser(config / target)(target => reportNameParser(allReportNames(target))).parsed
    val filteredReports = filterReportsIfReportNameIdSelected(allReports((config / target).value), selectedReportName)
    val reportsPaths = filteredReports.map(_.path.getName)
    reportsPaths.headOption.foreach { folderName =>
      val opts = toShortOptionAndValue("ro" -> folderName) ++ toShortOptionAndValue("rf" -> (config / target).value.getPath)
      val fork = new Fork("java", Some("io.gatling.app.Gatling"))
      val classpath = buildClassPathArgument((config / dependencyClasspath).value.map(_.data))
      fork(forkOptionsWithRunJVMOptions(classpath), opts)
    }
  }

  def copyConfigurationFiles(resourceDirectory: File, updateReport: UpdateReport): Set[File] = {
    val gatlingConf = extractFromCoreJar(updateReport, "gatling-defaults.conf") { source =>
      val target = resourceDirectory / "gatling.conf"
      generateCommentedConfigFile(source, target)
    }

    val gatlingAkkaConf = extractFromCoreJar(updateReport, "gatling-akka-defaults.conf") { source =>
      val target = resourceDirectory / "gatling-akka.conf"
      generateCommentedConfigFile(source, target)
    }

    val recorderConf = extractFromRecorderJar(updateReport, "recorder-defaults.conf") { source =>
      val target = resourceDirectory / "recorder.conf"
      generateCommentedConfigFile(source, target)
    }
    Set(gatlingConf, gatlingAkkaConf, recorderConf)
  }

  def copyLogback(resourceDirectory: File, updateReport: UpdateReport): File =
    extractFromCoreJar(updateReport, "logback.dummy") { source =>
      val targetFile = resourceDirectory / "logback.xml"
      IO.copyFile(source, targetFile)
      targetFile
    }

  private def generateCommentedConfigFile(source: File, target: File): File = {
    val lines = IO.readLines(source)
    val commentedLines = lines.map { line =>
      if (line.endsWith("{") || line.endsWith("}")) line
      else line.replaceAll(LeadingSpacesRegex, "$1#")
    }
    IO.writeLines(target, commentedLines)
    target
  }

  private def buildClassPathArgument(classPathElements: Seq[File]): Seq[String] = {
    Seq("-cp", classPathElements.mkString(File.pathSeparator))
  }

  private def stateBasedParser[T, U](inputSource: SettingKey[T])(parserMaker: T => U) =
    Def.setting { state: State =>
      val extracted = Project.extract(state)
      val input = extracted.get(inputSource)
      parserMaker(input)
    }
}
