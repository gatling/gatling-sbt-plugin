/*
 * Copyright 2011-2022 GatlingCorp (https://gatling.io)
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

import scala.jdk.CollectionConverters._
import scala.util.{ Failure, Using }

import io.gatling.plugin.EnterprisePlugin
import io.gatling.plugin.exceptions._
import io.gatling.plugin.model.{ Simulation, SimulationStartResult }
import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.settings.gatling.EnterprisePluginTask._
import io.gatling.sbt.settings.gatling.EnterpriseUtils._

import sbt.{ Configuration, Def }
import sbt.Keys._
import sbt.internal.util.ManagedLogger

class TaskEnterpriseStart(config: Configuration, enterprisePackage: TaskEnterprisePackage) {

  private def logCreatedSimulation(logger: ManagedLogger, simulation: Simulation): Unit =
    logger.info(s"Created simulation named ${simulation.name} with ID '${simulation.id}'")

  private def logSimulationConfiguration(logger: ManagedLogger, simulationId: UUID): Unit =
    logger.info(
      s"""To start again the same simulation, specify -Dgatling.enterprise.simulationId=$simulationId, or add the configuration to your SBT settings, e.g.:
         |${config.id} / enterpriseSimulationId := s"$simulationId"
         |""".stripMargin
    )

  private def enterprisePluginTask(batchMode: Boolean): InitializeTask[EnterprisePlugin] = Def.taskDyn[EnterprisePlugin] {
    if (batchMode) batchEnterprisePluginTask(config)
    else interactiveEnterprisePluginTask(config)
  }

  private def startEnterpriseSimulation(batchMode: Boolean, simulationId: UUID) = Def.task {
    val logger = streams.value.log
    val systemProperties = (config / enterpriseSimulationSystemProperties).value.asJava
    val simulationClassname = configOptionalString(config / enterpriseSimulationClass).value
    val file = enterprisePackage.buildEnterprisePackage.value
    val enterprisePlugin = enterprisePluginTask(batchMode).value

    logger.info(s"Uploading and starting simulation...")
    Using(enterprisePlugin) { enterprisePlugin =>
      enterprisePlugin.uploadPackageAndStartSimulation(simulationId, systemProperties, simulationClassname.orNull, file)
    }.get
  }

  private def enterpriseSimulationCreate(batchMode: Boolean) = Def.task {
    val logger = streams.value.log
    val optionalDefaultSimulationTeamId = configOptionalString(config / enterpriseTeamId).value.map(UUID.fromString)
    val optionalPackageId = configOptionalString(config / enterprisePackageId).value.map(UUID.fromString)
    val simulationClassname = configOptionalString(config / enterpriseSimulationClass).value
    val systemProperties = (config / enterpriseSimulationSystemProperties).value.asJava
    val file = enterprisePackage.buildEnterprisePackage.value
    val enterprisePlugin = enterprisePluginTask(batchMode).value

    Using(enterprisePlugin) { enterprisePlugin =>
      logger.info("Creating and starting simulation...")
      enterprisePlugin.createAndStartSimulation(
        optionalDefaultSimulationTeamId.orNull,
        (config / organization).value,
        (config / normalizedName).value,
        simulationClassname.orNull,
        optionalPackageId.orNull,
        systemProperties,
        file
      )
    }.recoverWith {
      case e: SeveralTeamsFoundException =>
        val teams = e.getAvailableTeams.asScala
        logger.error(s"""More than 1 team were found while creating a simulation.
                        |Available teams:
                        |${teams.map(team => s"- ${team.id} (${team.name})").mkString("\n")}
                        |Specify the team you want to use with -Dgatling.enterprise.teamId=<teamId>, or add the configuration to your build.sbt, e.g.:
                        |${config.id} / enterpriseTeamId := ${teams.head.id}
                        |""".stripMargin)
        Failure(ErrorAlreadyLoggedException)
      case e: SeveralSimulationClassNamesFoundException =>
        val simulationClasses = e.getAvailableSimulationClassNames.asScala
        logger.error(
          s"""Several simulation classes were found
             |${simulationClasses.map("- " + _).mkString("\n")}
             |Specify the simulation you want to use with -Dgatling.enterprise.simulationClass=<className>, or add the configuration to your build.sbt, e.g.:
             |${config.id} / simulationClass := ${simulationClasses.head}
             |""".stripMargin
        )
        Failure(ErrorAlreadyLoggedException)
      case e: UserQuitException =>
        logger.warn(e.getMessage)
        Failure(ErrorAlreadyLoggedException)

      case e: SimulationStartException =>
        if (e.isCreated) {
          logCreatedSimulation(logger, e.getSimulation)
        }
        logSimulationConfiguration(logger, e.getSimulation.id)
        Failure(e.getCause)
    }.get
  }

  private val enterpriseSimulationStartResult = Def.inputTaskDyn {
    val enterpriseStartCommand = EnterpriseStartCommand.parser.parsed
    val settingSimulationId = configOptionalString(config / enterpriseSimulationId).value.map(UUID.fromString)
    val batchMode = enterpriseStartCommand.batchMode || System.console() == null

    settingSimulationId match {
      case Some(simulationId) => startEnterpriseSimulation(batchMode, simulationId)
      case _                  => enterpriseSimulationCreate(batchMode)
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
}
