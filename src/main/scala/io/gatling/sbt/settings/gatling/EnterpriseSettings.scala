/*
 * Copyright 2011-2024 GatlingCorp (https://gatling.io)
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

package io.gatling.sbt.settings.gatling

import java.{ lang => jl }

import io.gatling.sbt.GatlingKeys._

import sbt._
import sbt.Keys._

object EnterpriseSettings {
  private val onLoadBreakIfLegacyPluginFound = Def.setting {
    (onLoad in Global).value.andThen { state =>
      val foundLegacyFrontLinePlugin =
        Project.extract(state).structure.units.exists { case (_, build) =>
          build.projects.exists(
            _.autoPlugins.exists(_.label == "io.gatling.frontline.sbt.FrontLinePlugin")
          )
        }
      if (foundLegacyFrontLinePlugin) {
        throw new MessageOnlyException(
          s"""Plugin "io.gatling.frontline" % "sbt-frontline" is no longer needed, its functionality is now included in "io.gatling" % "gatling-sbt".
             |Please remove "io.gatling.frontline" % "sbt-frontline" from your plugins.sbt configuration file.
             |Please use the Gatling / enterprisePackage task instead of Test / assembly (or GatlingIt / enterprisePackage instead of It / assembly).
             |See https://gatling.io/docs/gatling/reference/current/extensions/sbt_plugin/ for more information.""".stripMargin
        )
      }
      state
    }
  }

  def settings(config: Configuration) = {
    val taskPackage = new TaskEnterprisePackage(config)
    val taskUpload = new TaskEnterpriseUpload(config, taskPackage)
    val taskStart = new TaskEnterpriseStart(config, taskPackage)
    val taskDeploy = new TaskEnterpriseDeploy(config, taskPackage)

    Seq(
      config / enterpriseUrl := new URL("https://cloud.gatling.io"),
      config / enterprisePackage := taskPackage.buildEnterprisePackage.value,
      config / enterpriseUpload := taskUpload.uploadEnterprisePackage.value,
      config / enterpriseStart := taskStart.enterpriseSimulationStart.evaluated,
      config / enterpriseDeploy := taskDeploy.enterpriseDeploy.value,
      config / enterprisePackageId := sys.props.get("gatling.enterprise.packageId").getOrElse(""),
      config / enterpriseTeamId := sys.props.get("gatling.enterprise.teamId").getOrElse(""),
      config / enterpriseSimulationId := sys.props.get("gatling.enterprise.simulationId").getOrElse(""),
      config / enterpriseControlPlaneUrl := sys.props
        .get("gatling.enterprise.controlPlaneUrl")
        .map(configString => new URL(configString)),
      config / waitForRunEnd := jl.Boolean.getBoolean("gatling.enterprise.waitForRunEnd"),
      config / enterpriseSimulationSystemProperties := Map.empty,
      config / enterpriseSimulationSystemPropertiesString := sys.props.get("gatling.enterprise.simulationSystemProperties").getOrElse(""),
      config / enterpriseSimulationEnvironmentVariables := Map.empty,
      config / enterpriseSimulationEnvironmentVariablesString := sys.props.get("gatling.enterprise.simulationEnvironmentVariables").getOrElse(""),
      config / enterpriseApiToken := sys.props.get("gatling.enterprise.apiToken").orElse(sys.env.get("GATLING_ENTERPRISE_API_TOKEN")).getOrElse(""),
      config / packageBin := (config / enterprisePackage).value, // If we directly use config / enterprisePackage for publishing, classifiers (-tests or -it) are not correctly handled.
      config / enterpriseSimulationClass := sys.props.get("gatling.enterprise.simulationClass").getOrElse("")
    )
  }

  private def legacyAssemblySetting(config: Configuration) = {
    val taskPackage = new TaskEnterprisePackage(config)
    config / assembly := taskPackage.legacyPackageEnterpriseJar.value
  }

  private val breakIfLegacyPluginFoundSetting =
    Global / onLoad := onLoadBreakIfLegacyPluginFound.value

  lazy val projectSettings: Seq[Def.Setting[_]] =
    Seq(legacyAssemblySetting(Test), legacyAssemblySetting(IntegrationTest), breakIfLegacyPluginFoundSetting)
}
