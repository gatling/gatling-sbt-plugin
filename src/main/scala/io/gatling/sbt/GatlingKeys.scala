package io.gatling.sbt

import sbt._

/** List of SBT keys for Gatling specific tasks. */
object GatlingKeys {

  val startRecorder = inputKey[Unit]("Start Gatling's Recorder")

  val lastReport = inputKey[Unit]("Open last Gatling report in browser")

  val copyConfigFiles = taskKey[Set[File]]("Copy Gatling's config files if missing")

  val copyLogbackXml = taskKey[File]("Copy Gatling's default logback.xml if missing")

  val generateReport = inputKey[Unit]("Generate report for a specific simulation")
}
