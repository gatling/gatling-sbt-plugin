/*
 * Copyright 2011-2022 GatlingCorp (https://gatling.io)
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

import _root_.io.gatling.sbt.settings.BaseSettings

import sbt._

/** List of SBT keys for Gatling specific tasks. */
object GatlingKeys {

  // Configurations
  val Gatling = config("gatling") extend Test
  val GatlingIt = config("gatling-it") extend IntegrationTest

  // OSS Tasks
  val startRecorder = inputKey[Unit]("Start Gatling's Recorder")
  val lastReport = inputKey[Unit]("Open last Gatling report in browser")
  val copyConfigFiles = taskKey[Set[File]]("Copy Gatling's config files if missing")
  val copyLogbackXml = taskKey[File]("Copy Gatling's default logback.xml if missing")
  val generateReport = inputKey[Unit]("Generate report for a specific simulation")

  // Enterprise Settings

  private def systemPropertyDescription(systemProperty: String): String =
    s"May be configured using `$systemProperty` system property"

  private val documentationReference =
    "See https://gatling.io/docs/gatling/reference/current/extensions/sbt_plugin/#working-with-gatling-enterprise-cloud for more information"

  val enterpriseUrl = settingKey[URL]("URL of Gatling Enterprise.")

  val enterpriseApiToken = settingKey[String](s"""API Token on Gatling Enterprise.
                                                 |Prefer configuration using `GATLING_ENTERPRISE_API_TOKEN` environment variable, or `gatling.enterprise.apiToken` system property.
                                                 |$documentationReference.
                                                 |""".stripMargin)

  val enterprisePackageId = settingKey[String](s"""Package ID on Gatling Enterprise (used by `enterpriseUpload` task).
                                                  |${systemPropertyDescription("gatling.enterprise.packageId")}.
                                                  |$documentationReference.
                                                  |""".stripMargin)

  val enterpriseSimulationClass = settingKey[String](s"""Simulation class name, used when creating a simulation.
                                                        |${systemPropertyDescription("gatling.enterprise.simulationClass")}.
                                                        |$documentationReference.
                                                        |""".stripMargin)

  val enterpriseTeamId = settingKey[String](s"""Team ID on Gatling Enterprise. Used as default team on simulation and package on creation.
                                               |${systemPropertyDescription("gatling.enterprise.teamId")}.
                                               |$documentationReference.
                                               |""".stripMargin)

  val enterpriseSimulationId =
    settingKey[String](s"""Simulation ID on Gatling Enterprise. Used by `enterpriseStart` (and `enterprisePackage` if `enterprisePackageId` isn't configured).
                          |${systemPropertyDescription("gatling.enterprise.simulationId")}.
                          |$documentationReference.
                          |""".stripMargin)

  val enterpriseSimulationSystemProperties = settingKey[Map[String, String]](
    s"""Provides additional system properties when starting a simulation, in addition to the ones which may already be defined for that simulation.
       |${systemPropertyDescription("gatling.enterprise.simulationSystemProperties")} with the format key1=value1,key2=value2
       |$documentationReference.
       |""".stripMargin
  )

  val enterpriseSimulationSystemPropertiesString = settingKey[String](
    s"""Alternative to enterpriseSimulationSystemProperties. Use the format key1=value1,key2=value2
       |${systemPropertyDescription("gatling.enterprise.simulationSystemProperties")}.
       |$documentationReference.
       |""".stripMargin
  )

  val enterpriseSimulationEnvironmentVariables = settingKey[Map[String, String]](
    s"""Provides additional environment variables when starting a simulation, in addition to the ones which may already be defined for that simulation.
       |${systemPropertyDescription("gatling.enterprise.simulationEnvironmentVariables")} with the format key1=value1,key2=value2
       |$documentationReference.
       |""".stripMargin
  )

  val enterpriseSimulationEnvironmentVariablesString = settingKey[String](
    s"""Alternative to enterpriseSimulationEnvironmentVariables. Use the format key1=value1,key2=value2.
       |${systemPropertyDescription("gatling.enterprise.simulationEnvironmentVariables")}.
       |$documentationReference.
       |""".stripMargin
  )

  // Enterprise Tasks
  val enterprisePackage = taskKey[File](s"""Build a package for Gatling Enterprise.
                                           |$documentationReference.
                                           |""".stripMargin)

  val enterpriseUpload = taskKey[Unit](
    s"""Upload a package for Gatling Enterprise. Require `enterpriseApiToken` and either `enterprisePackageId` or `enterpriseSimulationId` to be configured.
       |$documentationReference.
       |""".stripMargin
  )

  val enterpriseStart = inputKey[Unit](s"""Start a simulation for Gatling Enterprise. Require `enterpriseApiToken`.
                                          |In batch mode, if `enterpriseSimulationId` isn't configured, requires:
                                          |- `enterpriseSimulationClass` if there's more than one simulation class defined
                                          |- `enterpriseTeamId` if there's more than one team related to the API Token
                                          |- `enterpriseTeamId` if there's more than one team related to the API Token
                                          |- `enterprisePackageId` if you want to use an existing package on created simulation
                                          |$documentationReference.
                                          |""".stripMargin)

  val assembly = taskKey[File](
    "Builds a package for Gatling Enterprise (deprecated, please use 'Gatling / enterprisePackage' or 'GatlingIt / enterprisePackage' instead)."
  )

  // kept for compatibility with older versions
  def overrideDefaultJavaOptions(javaOptions: String*): Seq[String] = BaseSettings.overrideDefaultJavaOptions(javaOptions: _*)
}
