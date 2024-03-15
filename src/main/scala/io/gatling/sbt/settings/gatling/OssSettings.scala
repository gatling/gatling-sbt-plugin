/*
 * Copyright 2011-2024 GatlingCorp (https://gatling.io)
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

package io.gatling.sbt.settings.gatling

import java.io.File

import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.utils.ReportUtils._
import io.gatling.sbt.utils.StartRecorderUtils.{ addPackageIfNecessary, optionsParser, toShortOptionAndValue }

import sbt._
import sbt.Keys._

object OssSettings {
  private def forkOptionsWithRunJVMOptions(options: Seq[String]) =
    _root_.sbt.ForkOptions().withRunJVMOptions(options.toVector)

  private def recorderRunner(config: Configuration, parent: Configuration): Def.Initialize[InputTask[Int]] = Def.inputTask {
    // Parse args and add missing args if necessary
    val args = optionsParser.parsed
    val simulationsFolderArg = toShortOptionAndValue("sf" -> (config / scalaSource).value.getPath)
    val resourcesFolderArg = toShortOptionAndValue("rf" -> (config / resourceDirectory).value.getPath)
    val formatArg = toShortOptionAndValue("fmt", "scala")
    val allArgs = addPackageIfNecessary(args ++ simulationsFolderArg ++ resourcesFolderArg ++ formatArg, organization.value)

    val fork = new Fork("java", Some("io.gatling.recorder.GatlingRecorder"))
    val classpathElements = (parent / dependencyClasspath).value.map(_.data) :+ (config / resourceDirectory).value
    val classpath = buildClassPathArgument(classpathElements)
    fork(forkOptionsWithRunJVMOptions(classpath), allArgs)
  }

  private def cleanReports(folder: File): Unit = IO.delete(folder)

  private def generateGatlingReport(config: Configuration, parent: Configuration): Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val selectedReportName = stateBasedParser(config / target)(target => reportNameParser(allReportNames(target))).parsed
    val filteredReports = filterReportsIfReportNameIdSelected(allReports((config / target).value), selectedReportName)
    val reportsPaths = filteredReports.map(_.path.getName)
    reportsPaths.headOption.foreach { folderName =>
      val opts = toShortOptionAndValue("ro" -> folderName) ++ toShortOptionAndValue("rf" -> (config / target).value.getPath)
      val fork = new Fork("java", Some("io.gatling.app.Gatling"))
      val classpathElements = (parent / dependencyClasspath).value.map(_.data) :+ (config / resourceDirectory).value
      val classpath = buildClassPathArgument(classpathElements)
      fork(forkOptionsWithRunJVMOptions(classpath), opts)
    }
  }

  private def buildClassPathArgument(classPathElements: Seq[File]): Seq[String] =
    Seq("-cp", classPathElements.mkString(File.pathSeparator))

  private def stateBasedParser[T, U](inputSource: SettingKey[T])(parserMaker: T => U) =
    Def.setting { state: State =>
      val extracted = Project.extract(state)
      val input = extracted.get(inputSource)
      parserMaker(input)
    }

  def settings(config: Configuration, parent: Configuration) = Seq(
    config / startRecorder := recorderRunner(config, parent).evaluated,
    config / clean := cleanReports((config / target).value),
    config / generateReport := generateGatlingReport(config, parent).evaluated
  )
}
