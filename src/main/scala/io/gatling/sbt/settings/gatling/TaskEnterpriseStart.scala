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

import java.{ util => ju }
import java.util.UUID

import scala.jdk.CollectionConverters._
import scala.util.Try

import io.gatling.plugin.EnterprisePlugin
import io.gatling.plugin.util.PropertiesParserUtil
import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.settings.gatling.EnterprisePluginTask._
import io.gatling.sbt.settings.gatling.EnterpriseUtils._

import sbt.{ Configuration, Def }
import sbt.Keys._

class TaskEnterpriseStart(config: Configuration, enterprisePackage: TaskEnterprisePackage) extends RecoverEnterprisePluginException(config) {
  private def enterprisePluginTask(batchMode: Boolean): InitializeTask[EnterprisePlugin] = Def.taskDyn[EnterprisePlugin] {
    if (batchMode) batchEnterprisePluginTask(config)
    else interactiveEnterprisePluginTask(config)
  }

  private def startEnterpriseSimulation(batchMode: Boolean, simulationId: UUID) = Def.task {
    val logger = streams.value.log
    val systemProperties = selectProperties((config / enterpriseSimulationSystemProperties).value, (config / enterpriseSimulationSystemPropertiesString).value)
    val environmentVariables =
      selectProperties((config / enterpriseSimulationEnvironmentVariables).value, (config / enterpriseSimulationEnvironmentVariablesString).value)
    val simulationClassname = configOptionalString(config / enterpriseSimulationClass).value
    val file = enterprisePackage.buildEnterprisePackage.value
    val enterprisePlugin = enterprisePluginTask(batchMode).value

    Try {
      logger.info(s"Uploading and starting simulation...")
      enterprisePlugin.uploadPackageAndStartSimulation(simulationId, systemProperties, environmentVariables, simulationClassname.orNull, file)
    }.recoverWith(recoverEnterprisePluginException(logger)).get
  }

  private def createAndStartEnterpriseSimulation(batchMode: Boolean) = Def.task {
    val logger = streams.value.log
    val optionalDefaultSimulationTeamId = configOptionalString(config / enterpriseTeamId).value.map(UUID.fromString)
    val optionalPackageId = configOptionalString(config / enterprisePackageId).value.map(UUID.fromString)
    val simulationClassname = configOptionalString(config / enterpriseSimulationClass).value
    val systemProperties = selectProperties((config / enterpriseSimulationSystemProperties).value, (config / enterpriseSimulationSystemPropertiesString).value)
    val environmentVariables =
      selectProperties((config / enterpriseSimulationEnvironmentVariables).value, (config / enterpriseSimulationEnvironmentVariablesString).value)
    val file = enterprisePackage.buildEnterprisePackage.value
    val enterprisePlugin = enterprisePluginTask(batchMode).value

    Try {
      logger.info("Creating and starting simulation...")
      enterprisePlugin.createAndStartSimulation(
        optionalDefaultSimulationTeamId.orNull,
        (config / organization).value,
        (config / normalizedName).value,
        simulationClassname.orNull,
        optionalPackageId.orNull,
        systemProperties,
        environmentVariables,
        file
      )
    }.recoverWith(recoverEnterprisePluginException(logger)).get
  }

  private val enterpriseSimulationStartResult = Def.inputTaskDyn {
    val enterpriseStartCommand = EnterpriseStartCommand.parser.parsed
    val settingSimulationId = configOptionalString(config / enterpriseSimulationId).value.map(UUID.fromString)
    val batchMode = enterpriseStartCommand.batchMode || System.console() == null

    settingSimulationId match {
      case Some(simulationId) => startEnterpriseSimulation(batchMode, simulationId)
      case _                  => createAndStartEnterpriseSimulation(batchMode)
    }
  }

  val enterpriseSimulationStart: InitializeInputTask[Unit] = Def.inputTask {
    val baseUrl = (config / enterpriseUrl).value
    val settingSimulationId = (config / enterpriseSimulationId).value
    val logger = streams.value.log

    val simulationStartResult = enterpriseSimulationStartResult.evaluated

    if (simulationStartResult.createdSimulation) {
      logCreatedSimulation(logger, simulationStartResult.simulation)
    }

    if (settingSimulationId.isEmpty) {
      logSimulationConfiguration(logger, simulationStartResult.simulation.id)
    }

    val reportsUrl = baseUrl.toExternalForm + simulationStartResult.runSummary.reportsPath
    logger.success(s"Simulation successfully started; once running, reports will be available at $reportsUrl")
  }

  private def selectProperties(propertiesMap: Map[String, String], propertiesString: String): ju.Map[String, String] =
    if (propertiesMap == null || propertiesMap.isEmpty) {
      PropertiesParserUtil.parseProperties(propertiesString)
    } else {
      propertiesMap.asJava
    }
}
