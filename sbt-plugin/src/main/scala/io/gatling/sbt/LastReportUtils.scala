package io.gatling.sbt

import java.awt.{ GraphicsEnvironment, Desktop }

import sbt._
import sbt.complete.Parser
import sbt.complete.DefaultParsers._

object LastReportUtils {

  val reportFolderRegex = """(\w+)-(\d+)""".r

  def openInBrowser(location: URI): Unit = {
    if (!Desktop.isDesktopSupported || GraphicsEnvironment.isHeadless)
      throw new UnsupportedOperationException("Opening a report from SBT is currently not supported on your platform.")
    else
      Desktop.getDesktop.browse(location)
  }

  def simulationIdParser(allSimulationIds: Set[String]): Parser[Option[String]] =
    (token(Space) ~> ID.examples(allSimulationIds, check = true)).?

  def filterReportsIfSimulationIdSelected(allReports: Seq[Report], simulationId: Option[String]): Seq[Report] =
    simulationId match {
      case Some(id) => allReports.filter(_.simulationId == id)
      case None     => allReports
    }

  case class Report(path: File, simulationId: String, timestamp: String)

  implicit val reportOrdering = Ordering.fromLessThan[Report](_.timestamp > _.timestamp)

  def allReports(reportsFolder: File): Seq[Report] = {
    val filter = DirectoryFilter && new PatternFilter(reportFolderRegex.pattern)
    val allDirectories = (reportsFolder ** filter).get
    val reports = for {
      directory <- allDirectories
      reportFolderRegex(simulationId, timestamp) <- reportFolderRegex findFirstIn directory.getName
    } yield Report(directory, simulationId, timestamp)
    reports.toList.sorted
  }

  def allSimulationIds(reportsFolder: File): Set[String] = allReports(reportsFolder).map(_.simulationId).distinct.toSet
}
