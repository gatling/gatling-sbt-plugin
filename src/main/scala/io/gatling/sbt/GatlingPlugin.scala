package io.gatling.sbt

import sbt._
import sbt.Keys._
import sbt.Tests.{ Argument, Group }

import io.gatling.sbt.GatlingTasks._

object GatlingPlugin extends AutoPlugin {

  /**********************/
  /** AutoPlugin setup **/
  /**********************/

  val autoImport = GatlingKeys

  import autoImport._

  override def projectConfigurations = Seq(Gatling, IntegrationTest, GatlingIt)

  override def projectSettings = gatlingAllSettings

  /*******************************/
  /** Test framework definition **/
  /*******************************/

  val gatlingTestFramework = TestFramework("io.gatling.sbt.GatlingFramework")

  /**************/
  /** Settings **/
  /**************/

  lazy val gatlingSettings = inConfig(Gatling)(Defaults.testSettings ++ gatlingBaseSettings(Gatling, Test))

  lazy val gatlingItSettings = inConfig(GatlingIt)(Defaults.itSettings ++ Defaults.testTasks ++ gatlingBaseSettings(GatlingIt, IntegrationTest))

  lazy val gatlingAllSettings = gatlingSettings ++ gatlingItSettings

  /********************/
  /** Helper methods **/
  /********************/

  private def gatlingBaseSettings(config: Configuration, parent: Configuration) = Seq(
    testFrameworks in config += gatlingTestFramework,
    target in config := target.value / config.name,
    fullClasspath in config := (fullClasspath in parent).value.filterNot(_.data == (classDirectory in config).value),
    testOptions in config += Argument(gatlingTestFramework, "-m", "-rf", (target in config).value.getPath),
    javaOptions in config ++= overrideDefaultJavaOptions(),
    sourceDirectory in config := (sourceDirectory in parent).value,
    parallelExecution in config := false,
    fork in config := true,
    testGrouping in config := (testGrouping in config).value flatMap singleTestGroup,
    startRecorder in config := recorderRunner(config, parent).evaluated,
    clean in config := cleanReports((target in config).value),
    lastReport in config := openLastReport(config).evaluated,
    copyConfigFiles in config := copyConfigurationFiles((target in config).value, (resourceDirectory in config).value, (update in config).value, streams.value.log),
    copyLogbackXml in config := copyLogback((target in config).value, (resourceDirectory in config).value, (update in config).value, streams.value.log),
    generateReport in config := generateGatlingReport(config).evaluated)

  /**
   * Split test groups so that each test is in its own group.
   *
   * @param group the original group
   * @return the list of groups made up by moving each to its own group.
   */
  private def singleTestGroup(group: Group): Seq[Group] =
    group.tests map (test => Group(test.name, Seq(test), group.runPolicy))

}
