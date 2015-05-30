/**
 * Copyright 2011-2015 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
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

  lazy val gatlingItSettings = inConfig(GatlingIt)(Defaults.itSettings ++ Defaults.testTasks ++ gatlingBaseSettings(GatlingIt, IntegrationTest, filterClasspath = false))

  lazy val gatlingAllSettings = gatlingSettings ++ gatlingItSettings

  /********************/
  /** Helper methods **/
  /********************/

  private def gatlingBaseSettings(config: Configuration, parent: Configuration, filterClasspath: Boolean = true) = Seq(
    testFrameworks in config += gatlingTestFramework,
    target in config := target.value / config.name,
    fullClasspath in config := (if (filterClasspath) (fullClasspath in parent).value.filterNot(_.data == (classDirectory in config).value) else (fullClasspath in parent).value),
    testOptions in config += Argument(gatlingTestFramework, "-m", "-rf", (target in config).value.getPath),
    javaOptions in config ++= overrideDefaultJavaOptions(),
    sourceDirectory in config := (sourceDirectory in parent).value,
    parallelExecution in config := false,
    fork in config := true,
    testGrouping in config := (testGrouping in config).value flatMap singleTestGroup,
    startRecorder in config := recorderRunner(config, parent).evaluated,
    clean in config := cleanReports((target in config).value),
    lastReport in config := openLastReport(config).evaluated,
    copyConfigFiles in config := copyConfigurationFiles((resourceDirectory in config).value, (update in config).value),
    copyLogbackXml in config := copyLogback((resourceDirectory in config).value, (update in config).value),
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
