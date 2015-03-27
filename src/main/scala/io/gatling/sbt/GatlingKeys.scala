package io.gatling.sbt

import sbt._

import io.gatling.sbt.utils.PropertyUtils.DefaultJvmArgs

/** List of SBT keys for Gatling specific tasks. */
object GatlingKeys {

  // ----------- //
  // -- Tasks -- //
  // ----------- //

  val startRecorder = inputKey[Unit]("Start Gatling's Recorder")

  val lastReport = inputKey[Unit]("Open last Gatling report in browser")

  val copyConfigFiles = taskKey[Set[File]]("Copy Gatling's config files if missing")

  val copyLogbackXml = taskKey[File]("Copy Gatling's default logback.xml if missing")

  val generateReport = inputKey[Unit]("Generate report for a specific simulation")

  // -------------------- //
  // -- Configurations -- //
  // -------------------- //

  val Gatling = config("gatling") extend Test

  val GatlingIt = config("gatling-it") extend IntegrationTest

  // -------------------- //
  // -- Helper methods -- //
  // -------------------- //

  private val unPropagatedPropertiesRoots =
    List("java.", "sun.", "jline.", "file.", "awt.", "os.", "user.")

  def overrideDefaultJavaOptions(javaOptions: String*) =
    javaOptions ++ propagatedSystemProperties ++ DefaultJvmArgs

  private def isPropagatedSystemProperty(name: String) =
    !(unPropagatedPropertiesRoots.exists(name.startsWith) ||
      name == "line.separator" ||
      name == "path.separator" ||
      name == "gopherProxySet")

  private def property(key: String, value: String) = s"-D$key=$value"

  private def propagatedSystemProperties: Seq[String] =
    sys.props
      .filterKeys(isPropagatedSystemProperty)
      .map { case (key, value) => property(key, value) }
      .toSeq

}
