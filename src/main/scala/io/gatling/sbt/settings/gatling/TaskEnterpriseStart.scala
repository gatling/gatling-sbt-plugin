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

import scala.util._

import io.gatling.plugin.EnterprisePlugin
import io.gatling.plugin.model.RunSummary
import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.settings.gatling.EnterpriseUtils.InitializeInputTask
import io.gatling.sbt.settings.gatling.TaskEnterpriseStart.CommandArgs.CommandArgsParser

import sbt.{ Configuration, Def }
import sbt.Keys._
import sbt.complete.DefaultParsers._
import sbt.internal.util.ManagedLogger
import sbt.internal.util.complete.Parser

object TaskEnterpriseStart {
  object CommandArgs {
    private val Default = CommandArgs(simulationName = None, requireBatchMode = false)

    private val SimulationNameParser: Parser[String] = StringBasic.examples("<simulation name>")

    private val RequireBatchModeParser: Parser[CommandArgs => CommandArgs] =
      token("--batch-mode" ^^^ (_.copy(requireBatchMode = true)))

    private val NoBatchModeParser: Parser[CommandArgs => CommandArgs] =
      token("--no-batch-mode" ^^^ (_.copy(requireBatchMode = System.console() == null)))

    val CommandArgsParser: Parser[CommandArgs] =
      ((Space ~> (NoBatchModeParser | RequireBatchModeParser)).* ~ (Space ~> SimulationNameParser).?)
        .map { case (results, simulationName) =>
          results
            .foldLeft(Default) { (current, op) =>
              op.apply(current)
            }
            .copy(simulationName = simulationName)
        }
  }
  final case class CommandArgs(simulationName: Option[String], requireBatchMode: Boolean)
}

class TaskEnterpriseStart(config: Configuration, taskEnterpriseDeploy: TaskEnterpriseDeploy) extends RecoverEnterprisePluginException(config) {
  val enterpriseSimulationStart: InitializeInputTask[Unit] = Def.inputTaskDyn {
    val commandArgs = CommandArgsParser.parsed

    Def.task {
      val logger = streams.value.log
      val enterprisePlugin = EnterprisePluginTask.enterprisePluginTask(config, commandArgs.requireBatchMode).value
      val deploymentInfo = taskEnterpriseDeploy.enterpriseDeploy.value
      val waitForRunEndSetting = waitForRunEnd.value

      val runSummary = enterprisePlugin.startSimulation(commandArgs.simulationName.orNull, deploymentInfo)

      logStartResult(logger, runSummary, waitForRunEndSetting, baseUrl = (config / enterpriseUrl).value)

      maybeWaitForRunEnd(logger, enterprisePlugin, waitForRunEndSetting, runSummary)
    }
  }

  private def logStartResult(logger: ManagedLogger, runSummary: RunSummary, waitForRunEndSetting: Boolean, baseUrl: sbt.URL): Unit = {
    logSimulationConfiguration(logger, waitForRunEndSetting)

    val reportsUrl = baseUrl.toExternalForm + runSummary.reportsPath
    logger.success(s"Simulation successfully started; reports available at $reportsUrl")
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
