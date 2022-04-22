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

import java.io.File

import io.gatling.sbt.settings.gatling.EnterpriseUtils.InitializeTask
import io.gatling.sbt.utils._

import sbt.{ Configuration, Def, IntegrationTest, ModuleDescriptorConfiguration, Test }
import sbt.Keys._

class TaskEnterprisePackage(config: Configuration) {

  private val moduleDescriptorConfig = Def.task {
    val logger = streams.value.log
    moduleSettings.value match {
      case config: ModuleDescriptorConfiguration => config
      case x =>
        logger.error(s"gatling-sbt expected a ModuleDescriptorConfiguration, but got a ${x.getClass}")
        throw ErrorAlreadyLoggedException
    }
  }

  val buildEnterprisePackage: InitializeTask[File] = Def.task {
    val moduleDescriptor = moduleDescriptorConfig.value

    val DependenciesAnalysisResult(gatlingVersion, dependencies) = DependenciesAnalyzer.analyze(
      dependencyResolution.value,
      updateConfiguration.value,
      (update / unresolvedWarningConfiguration).value,
      config,
      streams.value.log,
      moduleDescriptor
    )

    val classesDirectories = (config / fullClasspath).value.map(_.data).filter(_.isDirectory)

    val jarName = s"${moduleName.value}-gatling-enterprise-${version.value}"

    FatJar
      .packageFatJar(moduleDescriptor.module, classesDirectories, gatlingVersion, dependencies, target.value, jarName)
  }

  val legacyPackageEnterpriseJar: InitializeTask[File] = Def.sequential(
    Def.task {
      val newCommand = config.id match {
        case Test.id            => "Gatling / enterprisePackage"
        case IntegrationTest.id => "GatlingIt / enterprisePackage"
        case _                  => "Gatling / enterprisePackage or GatlingIt / enterprisePackage"
      }
      streams.value.log.warn(
        s"""Task ${config.id} / assembly is deprecated and will be removed in a future version.
           |Please use $newCommand instead.
           |See https://gatling.io/docs/gatling/reference/current/extensions/sbt_plugin/ for more information.""".stripMargin
      )
    },
    buildEnterprisePackage
  )

}
