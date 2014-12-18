package io.gatling.sbt

import sbt._
import sbt.Keys._

import io.gatling.sbt.utils.CopyUtils._
import io.gatling.sbt.utils.LastReportUtils._
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
    fork(ForkOptions(bootJars = (dependencyClasspath in parent).value.map(_.data)), allArgs)
  }

  def cleanReports(folder: File): Unit = IO.delete(folder)

  def openLastReport(config: Configuration) = Def.inputTask {
    val selectedSimulationId = simulationIdParser(allSimulationIds((target in config).value)).parsed
    val filteredReports = filterReportsIfSimulationIdSelected(allReports((target in config).value), selectedSimulationId)
    val reportsPaths = filteredReports.map(_.path)
    reportsPaths.headOption.foreach(file => openInBrowser((file / "index.html").toURI))
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
}
