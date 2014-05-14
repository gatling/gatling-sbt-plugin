package io.gatling.sbt

import sbt._
import sbt.Keys._
import sbt.complete.DefaultParsers._
import sbt.complete.Parser

import io.gatling.sbt.Utils._

object GatlingTasks {

  val startRecorder = taskKey[Unit]("Start Gatling's Recorder")

  val lastReport = inputKey[Unit]("Open last Gatling report in browser")

  def recorderRunner(classpath: Seq[File], pkg: String, outputFolder: File): Unit = {
    val fork = new Fork("java", Some("io.gatling.recorder.GatlingRecorder"))
    val args = Map(
      "pkg" -> pkg,
      "of" -> outputFolder.getPath)
    fork(ForkOptions(bootJars = classpath), toShortArgumentList(args))
  }

  def cleanReports(folder: File): Unit = {
    IO.delete(folder)
  }

  private def allReports(reportsFolder: File): Seq[Report] = {
    val filter = DirectoryFilter && new PatternFilter(reportFolderRegex.pattern)
    val allDirectories = (reportsFolder ** filter).get
    val reports = for {
      directory <- allDirectories
      reportFolderRegex(simulationId, timestamp) <- reportFolderRegex findFirstIn directory.getName
    } yield Report(directory, simulationId, timestamp)
    reports.toList.sorted
  }

  private def allSimulationIds(reports: Seq[Report]): Set[String] = reports.map(_.simulationId).distinct.toSet

  // TODO : See if it can be done better with next SBT releases
  def openLastReport(config: Configuration): Def.Initialize[InputTask[Unit]] = {
      def simulationIdParser(allSimulationIds: Set[String]): Parser[Option[String]] =
        (token(Space) ~> ID.examples(allSimulationIds, check = true)).?

      def filterReportsIfSimulationIdSelected(allReports: Seq[Report], simulationId: Option[String]): Seq[Report] =
        simulationId match {
          case Some(id) => allReports.filter(_.simulationId == id)
          case None     => allReports
        }

    Def.inputTask {
      val selectedSimulationId = simulationIdParser(allSimulationIds(allReports((target in config).value))).parsed
      val filteredReports = filterReportsIfSimulationIdSelected(allReports((target in config).value), selectedSimulationId)
      val reportsPaths = filteredReports.map(_.path)
      reportsPaths.headOption.foreach(file => openInBrowser((file / "index.html").toURI))
    }
  }
}
