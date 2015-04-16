/**
 * Copyright 2011-2015 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
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

import sbt._
import sbt.Keys._

import io.gatling.sbt.utils.CopyUtils._
import io.gatling.sbt.utils.ReportUtils._
import io.gatling.sbt.utils.StartRecorderUtils._

object GatlingTasks {

  private val LeadingSpacesRegex = """^(\s+)"""

  def recorderRunner(config: Configuration, parent: Configuration) = Def.inputTask {
    // Parse args and add missing args if necessary
    val args = optionsParser.parsed
    val outputFolderArg = toShortOptionAndValue("of" -> (scalaSource in config).value.getPath)
    val requestBodiesFolderArg = toShortOptionAndValue("bdf" -> ((resourceDirectory in config).value / "bodies").getPath)
    val allArgs = addPackageIfNecessary(args ++ outputFolderArg ++ requestBodiesFolderArg, organization.value)

    val fork = new Fork("java", Some("io.gatling.recorder.GatlingRecorder"))
    val classpathElements = (dependencyClasspath in parent).value.map(_.data) ++ (resources in config).value
    val classpath = buildClassPathArgument(classpathElements)
    fork(ForkOptions(runJVMOptions = classpath), allArgs)
  }

  def cleanReports(folder: File): Unit = IO.delete(folder)

  def openLastReport(config: Configuration) = Def.inputTask {
    val selectedSimulationId = simulationIdParser(allSimulationIds((target in config).value)).parsed
    val filteredReports = filterReportsIfSimulationIdSelected(allReports((target in config).value), selectedSimulationId)
    val reportsPaths = filteredReports.map(_.path)
    reportsPaths.headOption.foreach(file => openInBrowser((file / "index.html").toURI))
  }

  def generateGatlingReport(config: Configuration) = Def.inputTask {
    val selectedReportName = reportNameParser(allReportNames((target in config).value)).parsed
    val filteredReports = filterReportsIfReportNameIdSelected(allReports((target in config).value), selectedReportName)
    val reportsPaths = filteredReports.map(_.path.getName)
    reportsPaths.headOption.foreach { folderName =>
      val opts = toShortOptionAndValue("ro" -> folderName) ++ toShortOptionAndValue("rf" -> (target in config).value.getPath)
      val fork = new Fork("java", Some("io.gatling.app.Gatling"))
      val classpath = buildClassPathArgument((dependencyClasspath in config).value.map(_.data))
      fork(ForkOptions(runJVMOptions = classpath), opts)
    }
  }

  def copyConfigurationFiles(resourceDirectory: File, updateReport: UpdateReport): Set[File] = {
    val gatlingConf = extractFromCoreJar(updateReport, "gatling-defaults.conf") { source =>
      val target = resourceDirectory / "gatling.conf"
      generateCommentedConfigFile(source, target)
    }
    val recorderConf = extractFromRecorderJar(updateReport, "recorder-defaults.conf") { source =>
      val target = resourceDirectory / "recorder.conf"
      generateCommentedConfigFile(source, target)
    }
    Set(gatlingConf, recorderConf)
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
}
