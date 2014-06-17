package io.gatling.sbt

import sbt._
import sbt.Keys._
import sbt.Tests.{ Argument, Group, SubProcess }

import io.gatling.sbt.GatlingTasks._

object GatlingPlugin extends Plugin {

  /*************/
  /** Configs **/
  /*************/

  val Gatling = config("gatling") extend Test

  val GatlingIt = config("gatling-it") extend IntegrationTest

  /*******************************/
  /** Test framework definition **/
  /*******************************/

  val gatlingTestFramework = TestFramework("io.gatling.sbt.GatlingFramework")

  /**************/
  /** Settings **/
  /**************/

  lazy val gatlingSettings = inConfig(Gatling)(Defaults.testSettings ++ gatlingBaseSettings(Gatling, Test))

  lazy val gatlingItSettings = inConfig(GatlingIt)(Defaults.itSettings ++ gatlingBaseSettings(GatlingIt, IntegrationTest))

  lazy val gatlingAllSettings = gatlingSettings ++ gatlingItSettings

  /********************/
  /** Helper methods **/
  /********************/

  private def gatlingBaseSettings(config: Configuration, parent: Configuration) = Seq(
    testFrameworks in config := Seq(gatlingTestFramework),
    target in config := target.value / config.name,
    testOptions in config += Argument("-m", "-rf", (target in config).value.getPath),
    sourceDirectory in config := (sourceDirectory in parent).value,
    parallelExecution in config := false,
    fork in config := true,
    testGrouping in config := (definedTests in config).value map singleTestGroup,
    startRecorder in config := recorderRunner(config, parent).evaluated,
    clean in config := cleanReports((target in config).value),
    lastReport in config := openLastReport(config).evaluated,
    copyConfigFiles in config := copyConfigurationFiles((target in config).value, (resourceDirectory in config).value, (update in config).value, streams.value.log))

  private def singleTestGroup(test: TestDefinition) = new Group(test.name, Seq(test), SubProcess(ForkOptions()))

}
