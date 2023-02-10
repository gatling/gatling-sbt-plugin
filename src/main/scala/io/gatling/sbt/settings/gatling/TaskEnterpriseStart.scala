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
import scala.util.{ Failure, Success, Try }

import io.gatling.plugin.EnterprisePlugin
import io.gatling.plugin.model.{ RunSummary, SimulationStartResult }
import io.gatling.plugin.util.PropertiesParserUtil
import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.settings.gatling.EnterprisePluginTask._
import io.gatling.sbt.settings.gatling.EnterpriseUtils._

import sbt.{ Configuration, Def }
import sbt.Keys._
import sbt.internal.util.ManagedLogger

class TaskEnterpriseStart(config: Configuration, enterprisePackage: TaskEnterprisePackage) extends RecoverEnterprisePluginException(config) {

  private val enterprisePluginTask = Def.inputTaskDyn[EnterprisePlugin] {
    val enterpriseStartCommand = EnterpriseStartCommand.parser.parsed
    val batchMode = enterpriseStartCommand.batchMode || System.console() == null
    if (batchMode) batchEnterprisePluginTask(config)
    else interactiveEnterprisePluginTask(config)
  }

  val enterpriseSimulationStart: InitializeInputTask[Unit] = Def.inputTask {
    val logger = streams.value.log
    val enterprisePlugin = enterprisePluginTask.evaluated
    val file = enterprisePackage.buildEnterprisePackage.value
    val simulationIdSetting = configOptionalString(config / enterpriseSimulationId).value.map(UUID.fromString)
    val simulationClassname = configOptionalString(config / enterpriseSimulationClass).value
    val systemProperties = selectProperties((config / enterpriseSimulationSystemProperties).value, (config / enterpriseSimulationSystemPropertiesString).value)
    val environmentVariables =
      selectProperties((config / enterpriseSimulationEnvironmentVariables).value, (config / enterpriseSimulationEnvironmentVariablesString).value)
    val waitForRunEndSetting = waitForRunEnd.value

    val simulationStartResult =
      Try {
        simulationIdSetting match {
          case Some(simulationId) =>
            logger.info(s"Uploading and starting simulation...")
            enterprisePlugin.uploadPackageAndStartSimulation(simulationId, systemProperties, environmentVariables, simulationClassname.orNull, file)
          case _ =>
            logger.info("Creating and starting simulation...")
            val defaultSimulationTeamId = configOptionalString(config / enterpriseTeamId).value.map(UUID.fromString)
            val packageId = configOptionalString(config / enterprisePackageId).value.map(UUID.fromString)
            val groupId = (config / organization).value
            val artifactId = (config / normalizedName).value
            enterprisePlugin.createAndStartSimulation(
              defaultSimulationTeamId.orNull,
              groupId,
              artifactId,
              simulationClassname.orNull,
              packageId.orNull,
              systemProperties,
              environmentVariables,
              file
            )
        }
      }.recoverWith(recoverEnterprisePluginException(logger)).get

    logStartResult(logger, simulationStartResult, simulationIdSetting, waitForRunEndSetting, baseUrl = (config / enterpriseUrl).value)

    maybeWaitForRunEnd(logger, enterprisePlugin, waitForRunEndSetting, simulationStartResult.runSummary)
  }

  private def selectProperties(propertiesMap: Map[String, String], propertiesString: String): ju.Map[String, String] =
    if (propertiesMap == null || propertiesMap.isEmpty) {
      PropertiesParserUtil.parseProperties(propertiesString)
    } else {
      propertiesMap.asJava
    }

  private def logStartResult(
      logger: ManagedLogger,
      simulationStartResult: SimulationStartResult,
      simulationIdSetting: Option[UUID],
      waitForRunEndSetting: Boolean,
      baseUrl: sbt.URL
  ): Unit = {
    if (simulationStartResult.createdSimulation) {
      logCreatedSimulation(logger, simulationStartResult.simulation)
    }

    logSimulationConfiguration(logger, simulationIdSetting, waitForRunEndSetting, simulationStartResult.simulation.id)

    val reportsUrl = baseUrl.toExternalForm + simulationStartResult.runSummary.reportsPath
    logger.success(s"Simulation successfully started; once running, reports will be available at $reportsUrl")
  }

  private def maybeWaitForRunEnd(
      logger: ManagedLogger,
      enterprisePlugin: EnterprisePlugin,
      waitForRunEnd: Boolean,
      startedRun: RunSummary
  ): Unit =
    if (waitForRunEnd) {
      Try(enterprisePlugin.waitForRunEnd(startedRun))
        .flatMap {
          case finishedRun if !finishedRun.status.successful =>
            Failure(new IllegalStateException("Simulation failed."))
          case _ =>
            Success(())
        }
        .recoverWith(recoverEnterprisePluginException(logger))
        .get
    }
}
