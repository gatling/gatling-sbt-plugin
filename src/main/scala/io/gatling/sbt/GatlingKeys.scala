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

package io.gatling.sbt

import _root_.io.gatling.plugin.ConfigurationConstants
import _root_.io.gatling.sbt.settings.BaseSettings

import sbt._

/** List of SBT keys for Gatling specific tasks. */
object GatlingKeys {
  // Configurations
  val Gatling = config("gatling") extend Test
  val GatlingIt = config("gatling-it") extend IntegrationTest

  // OSS Tasks
  val startRecorder = inputKey[Unit]("Start Gatling's Recorder")
  val generateReport = inputKey[Unit]("Generate report for a specific simulation")

  // Enterprise Settings

  private def systemPropertyDescription(systemProperty: String): String =
    s"May be configured using `$systemProperty` system property"

  private val documentationReference =
    "See https://docs.gatling.io/reference/integrations/build-tools/sbt-plugin/#running-your-simulations-on-gatling-enterprise-cloud for more information"

  val enterpriseApiUrl = settingKey[URL]("URL of the Gatling Enterprise API.")

  val enterpriseWebAppUrl = settingKey[URL]("URL of the Gatling Enterprise web app.")

  val enterpriseApiToken =
    settingKey[String](
      s"""API Token on Gatling Enterprise.
         |Prefer configuration using `${ConfigurationConstants.ApiToken.ENV_VAR}` environment variable, or `${ConfigurationConstants.ApiToken.SYS_PROP}` system property.
         |$documentationReference.
         |""".stripMargin
    )

  @deprecated
  val enterprisePackageId = settingKey[String]("Deprecated package ID setting")

  @deprecated
  val enterpriseSimulationId = settingKey[String]("Deprecated simulation ID setting")

  val enterpriseValidateSimulationId =
    settingKey[String](s"""Simulation ID on Gatling Enterprise. Used by `enterpriseDeploy` to validate a simulation ID was part of the deployment.
                          |${systemPropertyDescription(ConfigurationConstants.DeployOptions.ValidateSimulationId.SYS_PROP)}.
                          |$documentationReference.
                          |""".stripMargin)

  val enterpriseControlPlaneUrl =
    settingKey[Option[URL]](s"""(optional) URL of a control plane for Gatling Enterprise providing a private repository.
                               |If this parameter is provided, packages will be registered as private packages and uploaded through this private control plane.
                               |$documentationReference.
                               |""".stripMargin)

  val waitForRunEnd =
    settingKey[Boolean](
      s"""Wait for the result after starting the simulation on Gatling Enterprise, and complete with an error if the simulation ends with any error status.
         |${systemPropertyDescription(ConfigurationConstants.StartOptions.WaitForRunEnd.SYS_PROP)}.
         |$documentationReference.
         |""".stripMargin
    )

  // Enterprise Tasks
  val enterprisePackage = taskKey[File](s"""Build a package for Gatling Enterprise.
                                           |$documentationReference.
                                           |""".stripMargin)

  @deprecated
  val enterpriseUpload = taskKey[Unit](
    "The enterprise upload command is no longer supported. It has been replaced by the enterprise deploy command." +
      " Refer to the documentation for more information: https://docs.gatling.io/reference/integrations/build-tools/sbt-plugin/#deploying-on-gatling-enterprise"
  )

  val enterpriseDeploy = inputKey[Unit]("Deploy a package and configured simulations")

  val enterpriseStart = inputKey[Unit](s"""Start a simulation deployed with `enterpriseDeploy`. Require `enterpriseApiToken`.
                                          |In batch mode, simulation name is required as first argument.
                                          |$documentationReference.
                                          |""".stripMargin)

  val assembly = taskKey[File](
    "Builds a package for Gatling Enterprise (deprecated, please use 'Gatling / enterprisePackage' or 'GatlingIt / enterprisePackage' instead)."
  )

  // kept for compatibility with older versions
  def overrideDefaultJavaOptions(javaOptions: String*): Seq[String] = BaseSettings.overrideDefaultJavaOptions(javaOptions: _*)
}
