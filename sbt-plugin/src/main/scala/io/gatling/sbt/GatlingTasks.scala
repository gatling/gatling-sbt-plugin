package io.gatling.sbt

import sbt._

import io.gatling.sbt.Utils._

object GatlingTasks {

	val startRecorder = taskKey[Unit]("Start Recorder")

	val lastReport = taskKey[Unit]("Open last report in browser")

	def recorderRunner(classpath: Seq[File], pkg: String, outputFolder: File): Unit = {
		val fork = new Fork("java", Some("io.gatling.recorder.GatlingRecorder"))
		val args = Map(
			"pkg" -> pkg,
			"of" -> outputFolder.getPath)
		fork(ForkOptions(bootJars = classpath), toShortArgumentList(args))
	}

	def openLastReport(reportsFolder: File): Unit = {
		val allDirectories = (reportsFolder ** DirectoryFilter.&&(new PatternFilter(reportFolderRegex.pattern))).get
		val dirsSortedByDate = allDirectories.map(file => (file, reportFolderRegex.findFirstMatchIn(file.getPath).get.group(2))).sortWith(_._2 > _._2).map(_._1)
		dirsSortedByDate.headOption.foreach(file => openInBrowser((file / "index.html").toURI))
	}
}
