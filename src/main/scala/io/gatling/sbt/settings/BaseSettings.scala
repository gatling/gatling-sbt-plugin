/*
 * Copyright 2011-2025 GatlingCorp (https://gatling.io)
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

package io.gatling.sbt.settings

import scala.jdk.CollectionConverters._

import io.gatling.plugin.GatlingConstants
import io.gatling.plugin.util.SystemProperties
import io.gatling.sbt.GatlingPlugin.gatlingTestFramework
import io.gatling.sbt.settings.gatling._

import sbt._
import sbt.Keys._
import sbt.Tests.{ Argument, Group }

object BaseSettings {
  private[gatling] def overrideDefaultJavaOptions(javaOptions: String*): Seq[String] =
    // the JVM gives precedence to the rightmost values
    propagatedSystemProperties ++ GatlingConstants.DEFAULT_JVM_OPTIONS_GATLING.asScala ++ javaOptions

  private def property(key: String, value: String) = s"-D$key=$value"

  private def propagatedSystemProperties: Seq[String] =
    sys.props
      .filterKeys(SystemProperties.isSystemPropertyPropagated)
      .map { case (key, value) => property(key, value) }
      .toSeq

  /**
   * Split test groups so that each test is in its own group.
   *
   * @param group
   *   the original group
   * @return
   *   the list of groups made up by moving each to its own group.
   */
  private def singleTestGroup(group: Group): Seq[Group] =
    group.tests map (test => Group(test.name, Seq(test), group.runPolicy))

  def settings(config: Configuration, parent: Configuration): Seq[Def.Setting[_]] = Seq(
    config / testFrameworks := Seq(gatlingTestFramework),
    config / target := target.value / config.name,
    config / testOptions += Argument(gatlingTestFramework, "-rf", (config / target).value.getPath),
    config / javaOptions ++= overrideDefaultJavaOptions(),
    config / parallelExecution := false,
    config / fork := true,
    config / testGrouping := (config / testGrouping).value flatMap singleTestGroup
  ) ++ OssSettings.settings(config, parent) ++ EnterpriseSettings.settings(config)
}
