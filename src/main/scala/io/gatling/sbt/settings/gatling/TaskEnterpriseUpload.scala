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

package io.gatling.sbt.settings.gatling

import io.gatling.sbt.settings.gatling.EnterpriseUtils._

import sbt._

class TaskEnterpriseUpload(config: Configuration) extends RecoverEnterprisePluginException(config) {
  val uploadEnterprisePackage: InitializeTask[Unit] = Def.task {
    throw new NotImplementedError(
      "The enterprise upload command is no longer supported. It has been replaced by the enterprise deploy command." +
        " Refer to the documentation for more information: https://docs.gatling.io/reference/integrations/build-tools/sbt-plugin/#deploying-on-gatling-enterprise"
    )
  }
}
