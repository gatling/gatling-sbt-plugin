package io.gatling.sbt.utils

import java.awt.{ Desktop, GraphicsEnvironment }

import sbt._
import sbt.complete.DefaultParsers._
import sbt.complete.Parser

private[gatling] object LastReportUtils {

  /** Regex matching a simulation ID (<name>-<timestamp>). */
  val reportFolderRegex = """(\w+)-(\d+)""".r

  /**
   * Opens the selected URI in the default web browser.
   * @param location the URI to open.
   */
  def openInBrowser(location: URI): Unit = {
    if (!Desktop.isDesktopSupported || GraphicsEnvironment.isHeadless)
      throw new UnsupportedOperationException("Opening a report from SBT is currently not supported on your platform.")
    else
      Desktop.getDesktop.browse(location)
  }

  /**
   * Builds the Parser matching one of the existing simulation IDs, or none.
   * @param allSimulationIds the list of all currently existing simulation IDs.
   * @return the built parser.
   */
  def simulationIdParser(allSimulationIds: Set[String]): Parser[Option[String]] =
    (token(Space) ~> ID.examples(allSimulationIds, check = true)).?

  /**
   * Filters out the reports using the selected simulationId, if any.
   * @param allReports the list of all reports
   * @param simulationId the possibly selected simulation ID.
   * @return the filtered (or not) reports.
   */
  def filterReportsIfSimulationIdSelected(allReports: Seq[Report], simulationId: Option[String]): Seq[Report] =
    simulationId match {
      case Some(id) => allReports.filter(_.simulationId == id)
      case None     => allReports
    }

  /**
   * A Gatling report.
   *
   * @param path the path of the report root folder.
   * @param simulationId the simulation ID for this report.
   * @param timestamp the timestamp of this report.
   */
  case class Report(path: File, simulationId: String, timestamp: String)

  /** Orders reports from most recent to oldest. */
  implicit val reportOrdering = Ordering.fromLessThan[Report](_.timestamp > _.timestamp)

  /**
   * Extracts the list of all reports, sorted by timestamp (desc),
   * found in the reports folder.
   *
   * @param reportsFolder the reports folder's path
   * @return the list of all reports.
   */
  def allReports(reportsFolder: File): Seq[Report] = {
    val filter = DirectoryFilter && new PatternFilter(reportFolderRegex.pattern)
    val allDirectories = (reportsFolder ** filter).get
    val reports = for {
      directory <- allDirectories
      reportFolderRegex(simulationId, timestamp) <- reportFolderRegex findFirstIn directory.getName
    } yield Report(directory, simulationId, timestamp)
    reports.toList.sorted
  }

  /**
   * Extracts the list of all simulation IDs of all the reports
   * found in the reports folder.
   *
   * @param reportsFolder the reports folder path
   * @return the list of all simulation IDs.
   */
  def allSimulationIds(reportsFolder: File): Set[String] = allReports(reportsFolder).map(_.simulationId).distinct.toSet
}
