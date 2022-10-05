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
import scala.util.{ Failure, Try }

import io.gatling.plugin.exceptions._
import io.gatling.plugin.model.Simulation

import sbt.Configuration
import sbt.internal.util.ManagedLogger

class RecoverEnterprisePluginException(config: Configuration) {
  protected def recoverEnterprisePluginException[U](logger: ManagedLogger): PartialFunction[Throwable, Try[U]] = {
    case e: UnsupportedJavaVersionException =>
      logger.error(
        s"""${e.getMessage}
           |In order to target the supported Java version, please use the following sbt settings:
           |scalacOptions ++= Seq("-target:${e.supportedVersion}", "-release", "${e.supportedVersion}")
           |javacOptions ++= Seq("--release", "${e.supportedVersion}")
           |See also the Scala compiler documentation: https://docs.scala-lang.org/overviews/compiler-options/index.html , and the Java compiler documentation: https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html#compiling-for-earlier-releases-of-the-platform
           |Alternatively, the reported class may come from your project's dependencies, published targeting Java ${e.version}. In this case you need to use dependencies which target Java ${e.supportedVersion} or lower.
           |""".stripMargin
      )
      Failure(ErrorAlreadyLoggedException)
    case e: SeveralTeamsFoundException =>
      val teams = e.getAvailableTeams.asScala
      logger.error(s"""More than 1 team were found while creating a simulation.
                      |Available teams:
                      |${teams.map(team => s"- ${team.id} (${team.name})").mkString("\n")}
                      |Specify the team you want to use with -Dgatling.enterprise.teamId=<teamId>, or add the configuration to your build.sbt, e.g.:
                      |${config.id} / enterpriseTeamId := "${teams.head.id}"
                      |""".stripMargin)
      Failure(ErrorAlreadyLoggedException)
    case e: SeveralSimulationClassNamesFoundException =>
      val simulationClasses = e.getAvailableSimulationClassNames.asScala
      logger.error(
        s"""Several simulation classes were found
           |${simulationClasses.map("- " + _).mkString("\n")}
           |Specify the simulation you want to use with -Dgatling.enterprise.simulationClass=<className>, or add the configuration to your build.sbt, e.g.:
           |${config.id} / simulationClass := "${simulationClasses.head}"
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
  }

  protected def logCreatedSimulation(logger: ManagedLogger, simulation: Simulation): Unit =
    logger.info(s"Created simulation named ${simulation.name} with ID '${simulation.id}'")

  protected def logSimulationConfiguration(logger: ManagedLogger, simulationId: UUID): Unit =
    logger.info(
      s"""To start again the same simulation, specify -Dgatling.enterprise.simulationId=$simulationId, or add the configuration to your SBT settings, e.g.:
         |${config.id} / enterpriseSimulationId := "$simulationId"
         |""".stripMargin
    )
}
