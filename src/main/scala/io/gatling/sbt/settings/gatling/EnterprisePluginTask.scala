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

import io.gatling.plugin.{ BatchEnterprisePlugin, EnterprisePlugin, EnterprisePluginProvider, PluginConfiguration }
import io.gatling.plugin.model.BuildTool
import io.gatling.sbt.BuildInfo
import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.settings.gatling.EnterprisePluginIO._
import io.gatling.sbt.settings.gatling.EnterpriseUtils.InitializeTask

import sbt._

object EnterprisePluginTask {
  def batchEnterprisePluginTask(config: Configuration): InitializeTask[BatchEnterprisePlugin] = Def.task {
    val configuration = pluginConfiguration(config, requireBatchMode = true).value
    EnterprisePluginProvider.getBatchInstance(configuration)
  }

  def enterprisePluginTask(config: Configuration, requireBatchMode: Boolean): InitializeTask[EnterprisePlugin] = Def.task {
    val configuration = pluginConfiguration(config, requireBatchMode).value
    EnterprisePluginProvider.getInstance(configuration)
  }

  private def pluginConfiguration(config: Configuration, requireBatchMode: Boolean): InitializeTask[PluginConfiguration] =
    Def.task {
      val url = (config / enterpriseUrl).value
      val apiToken = (config / enterpriseApiToken).value
      val privateControlPlaneUrl = (config / enterpriseControlPlaneUrl).value
      val pluginIO = enterprisePluginIOTask.value
      val logger = enterprisePluginLoggerTask.value

      if (apiToken.isEmpty) {
        logger.error(
          s"""An API token is required to call the Gatling Enterprise server; see https://gatling.io/docs/enterprise/cloud/reference/admin/api_tokens/ and create a token with the role 'Configurer'.
             |You can then set your API token's value in the environment variable GATLING_ENTERPRISE_API_TOKEN, pass it with -Dgatling.enterprise.apiToken=<apiToken>, or add the configuration to your SBT settings, e.g.:
             |${config.id} / enterpriseApiToken := MY_API_TOKEN_VALUE""".stripMargin
        )
        throw ErrorAlreadyLoggedException
      }

      new PluginConfiguration(url, apiToken, privateControlPlaneUrl.orNull, BuildTool.SBT, BuildInfo.version, requireBatchMode, pluginIO)
    }
}
