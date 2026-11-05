/*
 * Copyright 2011-2026 GatlingCorp (https://gatling.io)
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

import scala.jdk.CollectionConverters._

import io.gatling.plugin.GatlingConstants
import io.gatling.sbt.Compat
import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.utils.ReportUtils._
import io.gatling.sbt.utils.StartRecorderUtils.{ addOptionIfNecessary, addPackageIfNecessary, optionsParser, toShortOptionAndValue }
import io.gatling.shared.cli.RecorderCliOptions

import sbt._
import sbt.Keys._

object OssSettings {
  private def forkOptionsWithRunJVMOptions(options: Seq[String]) =
    _root_.sbt.ForkOptions().withRunJVMOptions(options.toVector)

  private def recorderRunner(config: Configuration, parent: Configuration): Def.Initialize[InputTask[Int]] = Def.inputTask {
    // Parse args and add missing args if necessary
    // The Recorder keeps the last value of a repeated option, so defaults are only added when the user didn't specify them
    val args = optionsParser.parsed
    val withSimulationsFolder = addOptionIfNecessary(args, RecorderCliOptions.SimulationsFolder, (config / scalaSource).value.getPath)
    val withResourcesFolder = addOptionIfNecessary(withSimulationsFolder, RecorderCliOptions.ResourcesFolder, (config / resourceDirectory).value.getPath)
    val withFormat = addOptionIfNecessary(withResourcesFolder, RecorderCliOptions.Format, "scala")
    val allArgs = addPackageIfNecessary(withFormat, organization.value)

    val fork = new Fork("java", Some("io.gatling.recorder.GatlingRecorder"))
    val classpathElements = Compat.toFiles((parent / dependencyClasspath).value, fileConverter.value) :+ (config / resourceDirectory).value
    val classpath = buildClassPathArgument(classpathElements)
    fork(forkOptionsWithRunJVMOptions(classpath), allArgs)
  }

  private def cleanReports(folder: File): Unit = IO.delete(folder)

  private def generateGatlingReport(config: Configuration, parent: Configuration): Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val selectedReportName = reportNameParserFor(config).parsed
    val reportsFolder = (config / target).value
    val logger = streams.value.log
    val classpathElements = Compat.toFiles((parent / dependencyClasspath).value, fileConverter.value) :+ (config / resourceDirectory).value
    val filteredReports = filterReportsIfReportNameIdSelected(allReports(reportsFolder), selectedReportName)

    filteredReports.headOption match {
      case Some(report) =>
        val opts = toShortOptionAndValue("ro" -> report.path.getName) ++ toShortOptionAndValue("rf" -> reportsFolder.getPath)
        val fork = new Fork("java", Some("io.gatling.app.Gatling"))
        val classpath = buildClassPathArgument(classpathElements)
        val exitCode = fork(forkOptionsWithRunJVMOptions(classpath ++ GatlingConstants.DEFAULT_JVM_OPTIONS_GATLING.asScala), opts)
        if (exitCode != 0) {
          logger.error(s"Failed to generate the report for ${report.name} (exit code $exitCode)")
          throw ErrorAlreadyLoggedException
        }
      case None =>
        logger.warn(s"No report to generate: no simulation results found in $reportsFolder")
    }
  }

  private def buildClassPathArgument(classPathElements: Seq[File]): Seq[String] =
    Seq("-cp", classPathElements.mkString(File.pathSeparator))

  // The reports folder is resolved in the scope of the task (and not from the current project of the state), so that it's the right one
  // when running e.g. `sub/Gatling/generateReport` from another project. It's listed when parsing, so that newly generated reports are offered.
  private def reportNameParserFor(config: Configuration) =
    Def.setting {
      val reportsFolder = (config / target).value
      (_: State) => reportNameParser(allReportNames(reportsFolder))
    }

  def settings(config: Configuration, parent: Configuration) = Seq(
    config / startRecorder := recorderRunner(config, parent).evaluated,
    // `clean` deletes report folders as a side effect and must not be memoised by sbt 2.x's task cache
    // (otherwise a second config's clean can reuse the first's cached result and skip the deletion).
    config / clean := Compat.uncached(cleanReports((config / target).value)),
    config / generateReport := generateGatlingReport(config, parent).evaluated
  )
}
