/*
 * Copyright 2011-2026 GatlingCorp (https://gatling.io)
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
import io.gatling.sbt.Compat
import io.gatling.sbt.GatlingKeys._

import sbt._
import sbt.Keys._

object EnterpriseSettings {
  def settings(config: Configuration) = {
    val taskPackage = new TaskEnterprisePackage(config)
    val taskUpload = new TaskEnterpriseUpload(config)
    val taskDeploy = new TaskEnterpriseDeploy(config, taskPackage)
    val taskStart = new TaskEnterpriseStart(config, taskDeploy)

    Seq(
      config / enterpriseApiUrl := new URI(ConfigurationConstants.ApiUrl.value()).toURL,
      config / enterpriseWebAppUrl := new URI(ConfigurationConstants.WebAppUrl.value()).toURL,
      config / enterprisePackage := Compat.uncached(taskPackage.buildEnterprisePackage.value),
      config / enterpriseUpload := taskUpload.uploadEnterprisePackage.value,
      config / enterpriseDeploy := taskDeploy.enterpriseDeploy.evaluated,
      config / enterpriseStart := taskStart.enterpriseSimulationStart.evaluated,
      config / enterpriseValidateSimulationId := Option(ConfigurationConstants.DeployOptions.ValidateSimulationId.value()).getOrElse(""),
      config / enterpriseControlPlaneUrl := Option(ConfigurationConstants.ControlPlaneUrl.value())
        .map(configString => new URI(configString).toURL),
      config / waitForRunEnd := ConfigurationConstants.StartOptions.WaitForRunEnd.value(),
      config / enterpriseApiToken := Option(ConfigurationConstants.ApiToken.value()).getOrElse(""),
      config / packageBin := Compat.uncached(
        Compat.toPackagedArtifact((config / enterprisePackage).value, fileConverter.value)
      ) // If we directly use config / enterprisePackage for publishing, classifiers (-tests or -it) are not correctly handled.
    )
  }
}
