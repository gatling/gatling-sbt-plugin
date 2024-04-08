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

import scala.util.Try

import io.gatling.plugin.deployment.DeploymentConfiguration
import io.gatling.plugin.model._
import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.settings.gatling.EnterpriseUtils._

import sbt._
import sbt.Keys._

class TaskEnterpriseDeploy(config: Configuration, enterprisePackage: TaskEnterprisePackage) extends RecoverEnterprisePluginException(config) {
  val enterpriseDeploy: InitializeTask[DeploymentInfo] = Def.task {
    val logger = streams.value.log
    val enterprisePlugin = EnterprisePluginTask.batchEnterprisePluginTask(config).value
    val packageFile = enterprisePackage.buildEnterprisePackage.value
    val descriptorFile = DeploymentConfiguration.fromBaseDirectory((config / baseDirectory).value)
    val artifactId = (config / name).value
    val controlPlaneUrl = (config / enterpriseControlPlaneUrl).value

    Try {
      enterprisePlugin.deployFromDescriptor(
        descriptorFile,
        packageFile,
        artifactId,
        controlPlaneUrl.isDefined
      )
    }.recoverWith(recoverEnterprisePluginException(logger)).get
  }
}
