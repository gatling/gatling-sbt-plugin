/*
 * Copyright 2011-2021 GatlingCorp (https://gatling.io)
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

import _root_.io.gatling.sbt.GatlingTasks._

import sbt._
import sbt.Keys._
import sbt.Tests.{ Argument, Group }

object GatlingPlugin extends AutoPlugin {

  /**
   * *******************
   * AutoPlugin setup
   * *******************
   */
  override val requires = plugins.JvmPlugin
  val autoImport = GatlingKeys

  import autoImport._

  override def projectConfigurations: Seq[Configuration] = Seq(Gatling, IntegrationTest, GatlingIt)

  override def projectSettings: Seq[Def.Setting[_]] = gatlingAllSettings

  /**
   * ****************************
   * Test framework definition
   * ****************************
   */
  val gatlingTestFramework = TestFramework("io.gatling.sbt.GatlingFramework")

  /**
   * ***********
   * Settings
   * ***********
   */
  lazy val gatlingSettings: Seq[Def.Setting[_]] =
    inConfig(Gatling)(
      Defaults.testTasks ++
        (forkOptions := Defaults.forkOptionsTask.value) ++
        gatlingBaseSettings(Gatling, Test)
    )

  lazy val gatlingItSettings: Seq[Def.Setting[_]] =
    inConfig(GatlingIt)(
      Defaults.itSettings ++
        Defaults.testTasks ++
        (forkOptions := Defaults.forkOptionsTask.value) ++
        gatlingBaseSettings(GatlingIt, IntegrationTest)
    )

  lazy val backwardCompatibilitySettings: Seq[Def.Setting[_]] =
    Seq(legacyAssemblySetting(Test), legacyAssemblySetting(IntegrationTest), breakIfLegacyPluginFoundSetting)

  lazy val gatlingAllSettings: Seq[Def.Setting[_]] =
    gatlingSettings ++ gatlingItSettings ++ backwardCompatibilitySettings

  /**
   * *****************
   * Helper methods
   * *****************
   */
  private def gatlingBaseSettings(config: Configuration, parent: Configuration) = Seq(
    config / testFrameworks := Seq(gatlingTestFramework),
    config / target := target.value / config.name,
    config / testOptions += Argument(gatlingTestFramework, "-rf", (config / target).value.getPath),
    config / javaOptions ++= overrideDefaultJavaOptions(),
    config / parallelExecution := false,
    config / fork := true,
    config / testGrouping := (config / testGrouping).value flatMap singleTestGroup,
    config / startRecorder := recorderRunner(config, parent).evaluated,
    config / clean := cleanReports((config / target).value),
    config / lastReport := openLastReport(config).evaluated,
    config / copyConfigFiles := copyConfigurationFiles((config / resourceDirectory).value, (config / update).value),
    config / copyLogbackXml := copyLogback((config / resourceDirectory).value, (config / update).value),
    config / generateReport := generateGatlingReport(config).evaluated,
    config / enterpriseUrl := new URL("https://cloud.gatling.io/api/public"),
    config / enterpriseAssembly := packageEnterpriseJar(config).value,
    config / enterprisePublish := publishEnterpriseJar(config).value,
    config / enterprisePackageId := "",
    config / enterpriseApiToken := sys.props.get("gatling.enterprise.apiToken").orElse(sys.env.get("GATLING_ENTERPRISE_API_TOKEN")).getOrElse("")
  )

  /**
   * Split test groups so that each test is in its own group.
   *
   * @param group the original group
   * @return the list of groups made up by moving each to its own group.
   */
  private def singleTestGroup(group: Group): Seq[Group] =
    group.tests map (test => Group(test.name, Seq(test), group.runPolicy))

  private def legacyAssemblySetting(config: Configuration) =
    config / assembly := legacyPackageEnterpriseJar(config).value

  private def breakIfLegacyPluginFoundSetting =
    Global / onLoad := onLoadBreakIfLegacyPluginFound.value
}
