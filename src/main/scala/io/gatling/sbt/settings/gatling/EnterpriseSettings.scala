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

import java.net.URI

import io.gatling.plugin.ConfigurationConstants
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
             |See https://docs.gatling.io/reference/integrations/build-tools/sbt-plugin/ for more information.""".stripMargin
        )
      }
      state
    }
  }

  def settings(config: Configuration) = {
    val taskPackage = new TaskEnterprisePackage(config)
    val taskUpload = new TaskEnterpriseUpload(config, taskPackage)
    val taskDeploy = new TaskEnterpriseDeploy(config, taskPackage)
    val taskStart = new TaskEnterpriseStart(config, taskDeploy)

    Seq(
      config / enterpriseApiUrl := new URI(ConfigurationConstants.ApiUrl.value()).toURL,
      config / enterpriseWebAppUrl := new URI(ConfigurationConstants.WebAppUrl.value()).toURL,
      config / enterprisePackage := taskPackage.buildEnterprisePackage.value,
      config / enterpriseUpload := taskUpload.uploadEnterprisePackage.value,
      config / enterpriseDeploy := taskDeploy.enterpriseDeploy.evaluated,
      config / enterpriseStart := taskStart.enterpriseSimulationStart.evaluated,
      config / enterprisePackageId := Option(ConfigurationConstants.UploadOptions.PackageId.value()).getOrElse(""),
      config / enterpriseSimulationId := Option(ConfigurationConstants.UploadOptions.SimulationId.value()).getOrElse(""),
      config / enterpriseControlPlaneUrl := Option(ConfigurationConstants.ControlPlaneUrl.value())
        .map(configString => new URI(configString).toURL),
      config / waitForRunEnd := ConfigurationConstants.StartOptions.WaitForRunEnd.value(),
      config / enterpriseApiToken := Option(ConfigurationConstants.ApiToken.value()).getOrElse(""),
      config / packageBin := (config / enterprisePackage).value // If we directly use config / enterprisePackage for publishing, classifiers (-tests or -it) are not correctly handled.
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
