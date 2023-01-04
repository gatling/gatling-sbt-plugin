/*
 * Copyright 2011-2023 GatlingCorp (https://gatling.io)
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

import io.gatling.plugin.client.http.HttpEnterpriseClient
import io.gatling.plugin.exceptions.UnsupportedClientException
import io.gatling.sbt.BuildInfo
import io.gatling.sbt.GatlingKeys._

import sbt.{ Configuration, Def }
import sbt.Keys.streams

object EnterpriseClient {
  def enterpriseClientTask(config: Configuration) = Def.task {
    val settingUrl = (config / enterpriseUrl).value
    val settingApiToken = (config / enterpriseApiToken).value
    val logger = streams.value.log

    if (settingApiToken.isEmpty) {
      logger.error(
        s"""An API token is required to call the Gatling Enterprise server; see https://gatling.io/docs/enterprise/cloud/reference/admin/api_tokens/ and create a token with the role 'Configurer'.
           |You can then set your API token's value in the environment variable GATLING_ENTERPRISE_API_TOKEN, pass it with -Dgatling.enterprise.apiToken=<apiToken>, or add the configuration to your SBT settings, e.g.:
           |${config.id} / enterpriseApiToken := MY_API_TOKEN_VALUE""".stripMargin
      )
      throw ErrorAlreadyLoggedException
    }

    try {
      new HttpEnterpriseClient(settingUrl, settingApiToken, BuildInfo.name, BuildInfo.version)
    } catch {
      case _: UnsupportedClientException =>
        logger.error(
          "Please update the Gatling SBT plugin to the latest version for compatibility with Gatling Enterprise. See https://gatling.io/docs/gatling/reference/current/extensions/sbt_plugin/ for more information about this plugin."
        )
        throw ErrorAlreadyLoggedException
    }
  }
}
