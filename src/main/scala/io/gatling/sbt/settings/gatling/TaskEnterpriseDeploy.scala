/*
 * Copyright 2011-2026 GatlingCorp (https://gatling.io)
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
import io.gatling.sbt.settings.gatling.TaskEnterpriseDeploy.CommandArgs.CommandArgsParser

import sbt._
import sbt.Keys._
import sbt.complete.DefaultParsers._
import sbt.internal.util.complete.Parser

object TaskEnterpriseDeploy {
  object CommandArgs {
    private val Default = CommandArgs(customFileName = None)

    private val PackageDescriptorFileNameParser: Parser[CommandArgs => CommandArgs] =
      token("--package-descriptor-filename") ~> Space ~> StringBasic
        .examples("<package descriptor filename> (inside .gatling/)")
        .map(customFileName => _.copy(customFileName = Some(customFileName)))

    val CommandArgsParser: Parser[CommandArgs] =
      (Space ~> PackageDescriptorFileNameParser).*.map { results =>
        results
          .foldLeft(Default) { (current, op) =>
            op.apply(current)
          }
      }
  }

  final case class CommandArgs(customFileName: Option[String])
}

class TaskEnterpriseDeploy(config: Configuration, enterprisePackage: TaskEnterprisePackage) extends RecoverEnterprisePluginException(config) {
  val enterpriseDeploy: InitializeInputTask[DeploymentInfo] = Def.inputTaskDyn {
    val commandArgs = CommandArgsParser.parsed

    enterpriseDeployTask(commandArgs.customFileName)
  }

  def enterpriseDeployTask(customFileName: Option[String]): InitializeTask[DeploymentInfo] = Def.task {
    val logger = streams.value.log
    val enterprisePlugin = EnterprisePluginTask.batchEnterprisePluginTask(config).value
    val packageFile = enterprisePackage.buildEnterprisePackage.value
    val descriptorFile = DeploymentConfiguration.fromBaseDirectory((config / baseDirectory).value, customFileName.orNull)
    val artifactId = (config / name).value
    val controlPlaneUrl = (config / enterpriseControlPlaneUrl).value
    val validateSimulationId = (config / enterpriseValidateSimulationId).value

    Try {
      if (validateSimulationId.isEmpty) {
        enterprisePlugin.deployFromDescriptor(
          descriptorFile,
          packageFile,
          artifactId,
          controlPlaneUrl.isDefined
        )
      } else {
        enterprisePlugin.deployFromDescriptor(
          descriptorFile,
          packageFile,
          artifactId,
          controlPlaneUrl.isDefined,
          validateSimulationId
        )
      }

    }.recoverWith(recoverEnterprisePluginException(logger)).get
  }
}
