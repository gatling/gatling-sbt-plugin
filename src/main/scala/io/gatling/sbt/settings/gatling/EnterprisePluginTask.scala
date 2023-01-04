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

import io.gatling.plugin.{ BatchEnterprisePlugin, BatchEnterprisePluginClient, InteractiveEnterprisePlugin, InteractiveEnterprisePluginClient }
import io.gatling.sbt.settings.gatling.EnterpriseClient.enterpriseClientTask
import io.gatling.sbt.settings.gatling.EnterprisePluginIO.{ enterprisePluginIOTask, enterprisePluginLoggerTask }
import io.gatling.sbt.settings.gatling.EnterpriseUtils.InitializeTask

import sbt.{ Configuration, Def }

object EnterprisePluginTask {
  def batchEnterprisePluginTask[E >: BatchEnterprisePlugin](config: Configuration): InitializeTask[E] = Def.task {
    val enterpriseClient = enterpriseClientTask(config).value
    val logger = enterprisePluginLoggerTask.value
    new BatchEnterprisePluginClient(enterpriseClient, logger)
  }

  def interactiveEnterprisePluginTask[E >: InteractiveEnterprisePlugin](config: Configuration): InitializeTask[E] = Def.task {
    val enterpriseClient = enterpriseClientTask(config).value
    val pluginIO = enterprisePluginIOTask.value
    new InteractiveEnterprisePluginClient(enterpriseClient, pluginIO)
  }
}
