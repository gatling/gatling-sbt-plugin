package io.gatling.sbt

import sbt._
import sbt.Keys._

import io.gatling.sbt.LastReportUtils._
import io.gatling.sbt.StartRecorderUtils._

object GatlingTasks {

  val startRecorder = inputKey[Unit]("Start Gatling's Recorder")

  val lastReport = inputKey[Unit]("Open last Gatling report in browser")

  // TODO : See if it's possible to circumvent the "illegal dynamic reference" compilation error
  def recorderRunner(config: Configuration, parent: Configuration) = Def.inputTask {
    // Parse args and add missing args if necessary
    val args = argsParser.parsed
    val outputFolderArg = toShortArgument("of" -> (scalaSource in config).value.getPath)
    val allArgs = addPackageIfNecessary(args ++ outputFolderArg, organization.value)

    val fork = new Fork("java", Some("io.gatling.recorder.GatlingRecorder"))
    fork(ForkOptions(bootJars = (dependencyClasspath in parent).value.map(_.data)), allArgs)
  }

  def cleanReports(folder: File): Unit = IO.delete(folder)

  // TODO : See if it's possible to circumvent the "illegal dynamic reference" compilation error
  def openLastReport(config: Configuration) = Def.inputTask {
    val selectedSimulationId = simulationIdParser(allSimulationIds((target in config).value)).parsed
    val filteredReports = filterReportsIfSimulationIdSelected(allReports((target in config).value), selectedSimulationId)
    val reportsPaths = filteredReports.map(_.path)
    reportsPaths.headOption.foreach(file => openInBrowser((file / "index.html").toURI))
  }
}
