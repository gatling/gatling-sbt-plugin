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
import java.util.UUID

import scala.collection.JavaConverters._

import io.gatling.plugin.util.OkHttpEnterpriseClient
import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.utils.{ DependenciesAnalysisResult, DependenciesAnalyzer, FatJar }

import sbt._
import sbt.Keys._

object EnterpriseSettings {
  private def moduleDescriptorConfig = Def.task {
    moduleSettings.value match {
      case config: ModuleDescriptorConfiguration => config
      case x =>
        throw new IllegalStateException(s"gatling-sbt expected a ModuleDescriptorConfiguration, but got a ${x.getClass}")
    }
  }

  private def buildEnterprisePackage(config: Configuration): Def.Initialize[Task[File]] = Def.task {
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

  private def legacyPackageEnterpriseJar(config: Configuration): Def.Initialize[Task[File]] = Def.sequential(
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
    buildEnterprisePackage(config)
  )

  private val PublicApiPath = "/api/public"

  private def httpEnterpriseClient(config: Configuration) = Def.task {
    val settingUrl = new URL((config / enterpriseUrl).value.toExternalForm + PublicApiPath)
    val settingApiToken = (config / enterpriseApiToken).value

    if (settingApiToken.isEmpty) {
      throw new IllegalStateException("Gatling / apiToken has not been specified")
    }

    new OkHttpEnterpriseClient(settingUrl, settingApiToken)
  }

  private def uploadEnterprisePackage(config: Configuration) = Def.task {
    val file = buildEnterprisePackage(config).value
    val settingPackageId = (config / enterprisePackageId).value
    val client = httpEnterpriseClient(config).value

    if (settingPackageId.isEmpty) {
      throw new IllegalStateException("Gatling / packageId has not been specified")
    }

    val settingPackageUuid = UUID.fromString(settingPackageId)
    client.uploadPackage(settingPackageUuid, file)
    streams.value.log.success("Successfully upload package")
  }

  private def startEnterpriseSimulation(config: Configuration) = Def.task {
    val settingSimulationId = (config / enterpriseSimulationId).value
    val settingSimulationSystemProperties = (config / enterpriseSimulationSystemProperties).value
    val client = httpEnterpriseClient(config).value
    val baseUrl = (config / enterpriseUrl).value
    val file = buildEnterprisePackage(config).value

    val systemProperties = settingSimulationSystemProperties.asJava
    val simulationAndRunSummary = if (settingSimulationId.isEmpty) {
      val defaultSimulationClassname = (config / enterpriseDefaultSimulationClassname).value
      if (defaultSimulationClassname.isEmpty) {
        throw new IllegalStateException("Gatling / enterpriseDefaultSimulationClassname has not been specified")
      }
      client.createAndStartSimulation(
        (config / organization).value,
        (config / normalizedName).value,
        defaultSimulationClassname,
        systemProperties,
        file
      )
    } else {
      val simulationId = UUID.fromString(settingSimulationId)
      client.startSimulation(simulationId, systemProperties, file)
    }

    val simulation = simulationAndRunSummary.simulation
    streams.value.log.success(
      s"""Setting for enterpriseUpload task:
         |enterprisePackageId := ${simulation.pkgId}
         |
         |Setting for enterpriseStart task next calls:
         |enterpriseSimulationId := ${simulation.id}
         |
         |Successfully start simulation named '${simulation.name}' run.
         |Live reports at ${baseUrl.toExternalForm + simulationAndRunSummary.runSummary.reportsPath}
         |""".stripMargin
    )
  }

  private def onLoadBreakIfLegacyPluginFound: Def.Initialize[State => State] = Def.setting {
    (onLoad in Global).value.andThen { state =>
      val foundLegacyFrontLinePlugin =
        Project.extract(state).structure.units.exists { case (_, build) =>
          build.projects.exists(
            _.autoPlugins.exists(_.label == "io.gatling.frontline.sbt.FrontLinePlugin")
          )
        }
      if (foundLegacyFrontLinePlugin) {
        val errorMessage =
          s"""Plugin "io.gatling.frontline" % "sbt-frontline" is no longer needed, its functionality is now included in "io.gatling" % "gatling-sbt".
             |Please remove "io.gatling.frontline" % "sbt-frontline" from your plugins.sbt configuration file.
             |Please use the Gatling / enterprisePackage task instead of Test / assembly (or GatlingIt / enterprisePackage instead of It / assembly).
             |See https://gatling.io/docs/gatling/reference/current/extensions/sbt_plugin/ for more information.""".stripMargin
        throw new MessageOnlyException(errorMessage)
      }
      state
    }
  }

  def settings(config: Configuration) = Seq(
    config / enterpriseUrl := new URL("https://cloud.gatling.io"),
    config / enterprisePackage := buildEnterprisePackage(config).value,
    config / enterpriseUpload := uploadEnterprisePackage(config).value,
    config / enterpriseStart := startEnterpriseSimulation(config).value,
    config / enterprisePackageId := "",
    config / enterpriseDefaultSimulationClassname := "",
    config / enterpriseSimulationId := "",
    config / enterpriseSimulationSystemProperties := Map.empty,
    config / enterpriseApiToken := sys.props.get("gatling.enterprise.apiToken").orElse(sys.env.get("GATLING_ENTERPRISE_API_TOKEN")).getOrElse(""),
    config / packageBin := (config / enterprisePackage).value // If we directly use config / enterprisePackage for publishing, classifiers (-tests or -it) are not correctly handled.
  )

  private def legacyAssemblySetting(config: Configuration) =
    config / assembly := legacyPackageEnterpriseJar(config).value

  private def breakIfLegacyPluginFoundSetting =
    Global / onLoad := onLoadBreakIfLegacyPluginFound.value

  lazy val projectSettings: Seq[Def.Setting[_]] =
    Seq(legacyAssemblySetting(Test), legacyAssemblySetting(IntegrationTest), breakIfLegacyPluginFoundSetting)
}
