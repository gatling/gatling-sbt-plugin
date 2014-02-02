package io.gatling.sbt

import sbt._
import sbt.Keys._

import java.awt.Desktop
import java.net.URI

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

}