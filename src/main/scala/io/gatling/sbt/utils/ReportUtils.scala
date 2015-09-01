/**
 * Copyright 2011-2015 GatlingCorp (http://gatling.io)
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
package io.gatling.sbt.utils

import java.awt.{ Desktop, GraphicsEnvironment }

import sbt._
import sbt.complete.DefaultParsers._
import sbt.complete.Parser

private[gatling] object ReportUtils {

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
   * Builds the Parser matching one of the existing reports' name, or none.
   * @param allReportNames the list of all currently existing reports.
   * @return the built parser.
   */
  def reportNameParser(allReportNames: Set[String]): Parser[Option[String]] =
    (token(Space) ~> ID.examples(allReportNames, check = true)).?

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
   * Filters out the reports using the selected report name, if any.
   * @param allReports the list of all reports
   * @param reportName the possibly selected report name.
   * @return the filtered (or not) reports.
   */
  def filterReportsIfReportNameIdSelected(allReports: Seq[Report], reportName: Option[String]): Seq[Report] =
    reportName match {
      case Some(name) => allReports.filter(_.name == name)
      case None       => allReports
    }

  /**
   * A Gatling report.
   *
   * @param path the path of the report root folder.
   * @param name the report's name
   * @param simulationId the simulation ID for this report.
   * @param timestamp the timestamp of this report.
   */
  case class Report(path: File, name: String, simulationId: String, timestamp: String)

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
    } yield Report(directory, directory.getName, simulationId, timestamp)
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

  /**
   * Extracts the list of all report names found in the reports folder.
   *
   * @param reportsFolder the reports folder path
   * @return the list of all report names.
   */
  def allReportNames(reportsFolder: File): Set[String] = allReports(reportsFolder).map(_.name).distinct.toSet
}
