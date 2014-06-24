package io.gatling.sbt

import sbt._

object GatlingKeys {

  val startRecorder = inputKey[Unit]("Start Gatling's Recorder")

  val lastReport = inputKey[Unit]("Open last Gatling report in browser")

  val copyConfigFiles = taskKey[Set[File]]("Copy Gatling's config files if missing")
}
