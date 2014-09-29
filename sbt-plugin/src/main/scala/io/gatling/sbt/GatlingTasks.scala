package io.gatling.sbt

import io.gatling.sbt.utils.{ StartRecorderUtils, LastReportUtils }
import sbt._
import sbt.Keys._

import LastReportUtils._
import StartRecorderUtils._

object GatlingTasks {

  /** List of all configuration files to be copied by [[copyConfigurationFiles]]. */
  val configFilesNames = Seq("gatling.conf", "recorder.conf")

  def recorderRunner(config: Configuration, parent: Configuration) = Def.inputTask {
    // Parse args and add missing args if necessary
    val args = optionsParser.parsed
    val outputFolderArg = toShortOptionAndValue("of" -> (scalaSource in config).value.getPath)
    val requestBodiesFolderArg = toShortOptionAndValue("rbf" -> ((resourceDirectory in config).value / "request-bodies").getPath)
    val allArgs = addPackageIfNecessary(args ++ outputFolderArg ++ requestBodiesFolderArg, organization.value)

    val fork = new Fork("java", Some("io.gatling.recorder.GatlingRecorder"))
    fork(ForkOptions(bootJars = (dependencyClasspath in parent).value.map(_.data)), allArgs)
  }

  def cleanReports(folder: File): Unit = IO.delete(folder)

  def openLastReport(config: Configuration) = Def.inputTask {
    val selectedSimulationId = simulationIdParser(allSimulationIds((target in config).value)).parsed
    val filteredReports = filterReportsIfSimulationIdSelected(allReports((target in config).value), selectedSimulationId)
    val reportsPaths = filteredReports.map(_.path)
    reportsPaths.headOption.foreach(file => openInBrowser((file / "index.html").toURI))
  }

  def copyConfigurationFiles(targetDir: File, resourceDirectory: File, updateReport: UpdateReport, logger: Logger): Set[File] =
    copyFromBundle(targetDir, resourceDirectory, updateReport, logger, file => configFilesNames.contains(file.getName))

  def copyLogback(targetDir: File, resourceDirectory: File, updateReport: UpdateReport, logger: Logger): File =
    copyFromBundle(targetDir, resourceDirectory, updateReport, logger, _.getName == "logback.xml").head

  private def copyFromBundle(unzipDir: File, targetDir: File,
                             updateReport: UpdateReport, logger: Logger,
                             copyCriteria: File => Boolean): Set[File] =
    updateReport.select(artifact = artifactFilter(new ExactFilter("gatling-bundle"))).headOption match {
      case Some(bundlePath) =>
        val tmpDir = unzipDir / "bundle-extract"
        val sourceFiles = IO.unzip(bundlePath, tmpDir).filter(copyCriteria)
        val sourcesAndTargets = sourceFiles.map(file => (file, targetDir / file.getName))
        val files = IO.copy(sources = sourcesAndTargets, overwrite = false)
        IO.delete(tmpDir)
        files.filter(copyCriteria)
      case None =>
        logger.error("Gatling's bundle not found, please add it to your dependencies.")
        Set.empty
    }
}
