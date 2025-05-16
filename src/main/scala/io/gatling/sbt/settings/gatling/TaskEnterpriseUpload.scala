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

import java.util.UUID

import scala.util.Try

import io.gatling.plugin.ConfigurationConstants
import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.settings.gatling.EnterpriseUtils._

import sbt._
import sbt.Keys.streams

class TaskEnterpriseUpload(config: Configuration, enterprisePackage: TaskEnterprisePackage) extends RecoverEnterprisePluginException(config) {
  val uploadEnterprisePackage: InitializeTask[Unit] = Def.task {
    val logger = streams.value.log
    val file = enterprisePackage.buildEnterprisePackage.value
    val settingPackageId = (config / enterprisePackageId).value
    val settingSimulationId = (config / enterpriseSimulationId).value
    val enterprisePlugin = EnterprisePluginTask.batchEnterprisePluginTask(config).value

    Try {
      if (settingPackageId.isEmpty && settingSimulationId.isEmpty) {
        logger.error(
          s"""A package ID is required to upload a package on Gatling Enterprise; see https://docs.gatling.io/reference/execute/cloud/user/package-conf/ , create a package and copy its ID.
             |You can then set your package ID value by passing it with -D${ConfigurationConstants.UploadOptions.PackageId.SYS_PROP}=<packageId>, or add the configuration to your SBT settings, e.g.:
             |${config.id} / enterprisePackageId := MY_PACKAGE_ID
             |
             |Alternately, if you don't configure a packageId, you can configure the simulationId of an existing simulation on Gatling Enterprise: your code will be uploaded to the package used by that simulation.
             |""".stripMargin
        )
        throw ErrorAlreadyLoggedException
      }

      if (settingPackageId.nonEmpty) {
        val packageUUID = UUID.fromString(settingPackageId)
        enterprisePlugin.uploadPackage(packageUUID, file)
      } else {
        val simulationId = UUID.fromString(settingSimulationId)
        enterprisePlugin.uploadPackageWithSimulationId(simulationId, file)
      }

      logger.success("Successfully upload package")
    }.recoverWith(recoverEnterprisePluginException(logger)).get
  }
}
