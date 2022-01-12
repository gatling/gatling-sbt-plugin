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
import scala.util.{ Failure, Try }

import io.gatling.plugin.{ EnterprisePlugin, EnterprisePluginClient, InteractiveEnterprisePluginClient }
import io.gatling.plugin.client.http.OkHttpEnterpriseClient
import io.gatling.plugin.exceptions.{ SeveralTeamsFoundException, SimulationStartException, UnsupportedClientException }
import io.gatling.plugin.io.{ PluginIO, PluginLogger, PluginScanner }
import io.gatling.plugin.model.Simulation
import io.gatling.sbt.BuildInfo
import io.gatling.sbt.GatlingKeys._
import io.gatling.sbt.utils.{ DependenciesAnalysisResult, DependenciesAnalyzer, FatJar }

import sbt._
import sbt.Keys._
import sbt.internal.util.ManagedLogger

object EnterpriseSettings {

  // MessageOnlyException seems to be logged multiple times when throw in an inner task
  // To prevent that, we explicitly log the error and throw a FeedbackProvidedException
  private object ErrorAlreadyLoggedException extends FeedbackProvidedException

  private def moduleDescriptorConfig = Def.task {
    val logger = streams.value.log
    moduleSettings.value match {
      case config: ModuleDescriptorConfiguration => config
      case x =>
        logger.error(s"gatling-sbt expected a ModuleDescriptorConfiguration, but got a ${x.getClass}")
        throw ErrorAlreadyLoggedException
    }
  }

  private def configOptionalString(key: SettingKey[String]) = Def.task {
    Option(key.value).filter(_.nonEmpty)
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

  private def enterpriseClientTask(config: Configuration) = Def.task {
    val settingUrl = new URL((config / enterpriseUrl).value.toExternalForm + PublicApiPath)
    val settingApiToken = (config / enterpriseApiToken).value
    val logger = streams.value.log

    if (settingApiToken.isEmpty) {
      logger.error(
        s"""An API token is required to call the Gatling Enterprise server; see https://gatling.io/docs/enterprise/cloud/reference/admin/api_tokens/ and create a token with the role 'Configurer'.
           |You can then set your API token's value in the environment variable GATLING_ENTERPRISE_API_TOKEN, pass it with -Dgatling.enterprise.apiToken=<apiToken>, or add the configuration to your SBT settings, e.g.:
           |${config.id} / enterpriseApiToken := MY_API_TOKEN_VALUE""".stripMargin
      )
      throw ErrorAlreadyLoggedException
    }

    try {
      OkHttpEnterpriseClient.getInstance(settingUrl, settingApiToken, BuildInfo.name, BuildInfo.version)
    } catch {
      case _: UnsupportedClientException =>
        logger.error(
          "Please update the Gatling SBT plugin to the latest version for compatibility with Gatling Enterprise. See https://gatling.io/docs/gatling/reference/current/extensions/sbt_plugin/ for more information about this plugin."
        )
        throw ErrorAlreadyLoggedException
    }
  }

  private def enterprisePluginTask(config: Configuration) = Def.task {
    val enterpriseClient = enterpriseClientTask(config).value
    val logger = enterprisePluginLogger.value
    val enterprisePlugin: EnterprisePlugin = new EnterprisePluginClient(enterpriseClient, logger)
    enterprisePlugin
  }

  private val enterprisePluginLogger = Def.task {
    val logger = streams.value.log
    new PluginLogger {
      override def info(message: String): Unit = logger.info(message)
      override def error(message: String): Unit = logger.error(message)
    }
  }

  private val enterprisePluginIOTask = Def.task {
    val interactiveService = interactionService.value
    val logger = enterprisePluginLogger.value
    new PluginIO {
      override def getLogger: PluginLogger = logger
      override def getScanner: PluginScanner = new PluginScanner {
        override def readString(): String = interactiveService.readLine("(ctrl+x+c to cancel) > ", mask = false).getOrElse("")
        override def readInt(): Int = Integer.parseInt(readString())
      }
    }
  }

  private def enterpriseInteractivePluginTask(config: Configuration) = Def.task {
    val enterpriseClient = enterpriseClientTask(config).value
    val pluginIO = enterprisePluginIOTask.value

    new InteractiveEnterprisePluginClient(enterpriseClient, pluginIO)
  }

  private def uploadEnterprisePackage(config: Configuration) = Def.task {
    val file = buildEnterprisePackage(config).value
    val settingPackageId = (config / enterprisePackageId).value
    val settingSimulationId = (config / enterpriseSimulationId).value
    val enterprisePlugin = enterprisePluginTask(config).value
    val logger = streams.value.log

    if (settingPackageId.isEmpty && settingSimulationId.isEmpty) {
      logger.error(
        s"""A package ID is required to upload a package on Gatling Enterprise; see https://gatling.io/docs/enterprise/cloud/reference/user/package_conf/, create a package and copy its ID.
           |You can then set your package ID value by passing it with -Dgatling.enterprise.packageId=<packageId>, or add the configuration to your SBT settings, e.g.:
           |${config.id} / enterprisePackageId := MY_PACKAGE_ID
           |
           |Alternately, if you don't configure a packageId, you can configure the simulationId of an existing simulation on Gatling Enterprise: your code will be uploaded to the package used by that simulation.
           |""".stripMargin
      )
      throw ErrorAlreadyLoggedException
    }

    if (settingPackageId.nonEmpty) {
      val packageId = UUID.fromString(settingPackageId)
      enterprisePlugin.uploadPackage(packageId, file)
    } else {
      val simulationId = UUID.fromString(settingSimulationId)
      enterprisePlugin.uploadPackageWithSimulationId(simulationId, file)
    }

    logger.success("Successfully upload package")
  }

  private def logCreatedSimulation(logger: ManagedLogger, simulation: Simulation): Unit =
    logger.info(s"Created simulation named ${simulation.name} with ID '${simulation.id}'")

  private def logSimulationConfiguration(logger: ManagedLogger, config: Configuration, simulationId: UUID): Unit =
    logger.info(
      s"""To start again the same simulation, specify -Dgatling.enterprise.simulationId=$simulationId, or add the configuration to your SBT settings, e.g.:
         |${config.id} / enterpriseSimulationId := s"$simulationId"
         |""".stripMargin
    )

  private def startEnterpriseSimulation(simulationId: UUID, config: Configuration) = Def.task {
    val logger = streams.value.log
    val systemProperties = (config / enterpriseSimulationSystemProperties).value.asJava
    val enterprisePlugin = enterprisePluginTask(config).value
    val file = buildEnterprisePackage(config).value

    logger.info(s"Uploading and starting simulation...")
    enterprisePlugin.uploadPackageAndStartSimulation(simulationId, systemProperties, file)
  }

  private def batchSimulationClassname(config: Configuration) = Def.task {
    val classNames: Seq[String] = (config / definedTests).value.map(_.name)
    val simulationClassname = (config / enterpriseSimulationClass).value
    val logger = streams.value.log

    if (simulationClassname.isEmpty) {
      val headClassname = classNames.head
      if (classNames.size > 1) {
        logger.error(
          s"""Several simulation classes were found
             |${classNames.map("- " + _).mkString("\n")}
             |Specify the simulation you want to use with -Dgatling.enterprise.simulationClass=<className>, or add the configuration to your build.sbt, e.g.:
             |${config.id} / simulationClass := $headClassname
             |""".stripMargin
        )
        throw ErrorAlreadyLoggedException
      }
      logger.info(s"Picking only available simulation class: $headClassname.")
      headClassname
    } else {
      simulationClassname
    }
  }

  private def batchCreateAndStartEnterpriseSimulation(config: Configuration) = Def.task {
    val logger = streams.value.log
    val enterprisePlugin = enterprisePluginTask(config).value
    val optionalDefaultSimulationTeamId = configOptionalString(config / enterpriseTeamId).value.map(UUID.fromString)
    val optionalPackageId = configOptionalString(config / enterprisePackageId).value.map(UUID.fromString)
    val simulationClassname = batchSimulationClassname(config).value
    val systemProperties = (config / enterpriseSimulationSystemProperties).value.asJava
    val file = buildEnterprisePackage(config).value

    logger.info("Creating and starting simulation...")

    Try(
      enterprisePlugin.createAndStartSimulation(
        optionalDefaultSimulationTeamId.orNull,
        (config / organization).value,
        (config / normalizedName).value,
        simulationClassname,
        optionalPackageId.orNull,
        systemProperties,
        file
      )
    ).recoverWith { case e: SeveralTeamsFoundException =>
      val teams = e.getAvailableTeams.asScala
      logger.error(s"""More than 1 team were found while creating a simulation.
                      |Available teams:
                      |${teams.map(team => s"- ${team.id} (${team.name})").mkString("\n")}
                      |Specify the team you want to use with -Dgatling.enterprise.teamId=<teamId>, or add the configuration to your build.sbt, e.g.:
                      |${config.id} / enterpriseTeamId := ${teams.head.id}
                      |""".stripMargin)
      Failure(ErrorAlreadyLoggedException)
    }
  }

  private def interactiveCreateOrStartEnterpriseSimulation(config: Configuration) = Def.task {
    val enterpriseInteractivePlugin = enterpriseInteractivePluginTask(config).value
    val groupId = (config / organization).value
    val artifactId = (config / normalizedName).value
    val file = buildEnterprisePackage(config).value
    val optionalTeamId = configOptionalString(config / enterpriseTeamId).value.map(UUID.fromString)
    val optionalSimulationClass = configOptionalString(config / enterpriseSimulationClass).value
    val classNames: Seq[String] = (config / definedTests).value.map(_.name)
    val optionalPackageId = configOptionalString(config / enterprisePackageId).value.map(UUID.fromString)
    val systemProperties: Map[String, String] = (config / enterpriseSimulationSystemProperties).value

    Try(
      enterpriseInteractivePlugin.createOrStartSimulation(
        optionalTeamId.orNull,
        groupId,
        artifactId,
        optionalSimulationClass.orNull,
        classNames.asJava,
        optionalPackageId.orNull,
        systemProperties.asJava,
        file
      )
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
        throw new MessageOnlyException(
          s"""Plugin "io.gatling.frontline" % "sbt-frontline" is no longer needed, its functionality is now included in "io.gatling" % "gatling-sbt".
             |Please remove "io.gatling.frontline" % "sbt-frontline" from your plugins.sbt configuration file.
             |Please use the Gatling / enterprisePackage task instead of Test / assembly (or GatlingIt / enterprisePackage instead of It / assembly).
             |See https://gatling.io/docs/gatling/reference/current/extensions/sbt_plugin/ for more information.""".stripMargin
        )
      }
      state
    }
  }

  private def enterpriseSimulationCreate(batchMode: Boolean, config: Configuration) = Def.taskDyn {
    val logger = streams.value.log
    val simulationStartResult = if (batchMode || System.console() == null) {
      batchCreateAndStartEnterpriseSimulation(config)
    } else {
      interactiveCreateOrStartEnterpriseSimulation(config)
    }

    Def.task {
      simulationStartResult.value.recoverWith { case e: SimulationStartException =>
        logCreatedSimulation(logger, e.getSimulation)
        logSimulationConfiguration(logger, config, e.getSimulation.id)
        Failure(e.getCause)
      }.get
    }
  }

  private def enterpriseSimulationStartResult(config: Configuration) = Def.inputTaskDyn {
    val enterpriseStartCommand = EnterpriseStartCommand.parser.parsed
    val settingSimulationId = enterpriseSimulationId.value

    if (settingSimulationId.nonEmpty) {
      val simulationId = UUID.fromString(settingSimulationId)
      startEnterpriseSimulation(simulationId, config)
    } else {
      enterpriseSimulationCreate(enterpriseStartCommand.batchMode, config)
    }
  }

  private def enterpriseSimulationStart(config: Configuration) = Def.inputTask {
    val baseUrl = (config / enterpriseUrl).value
    val settingSimulationId = (config / enterpriseSimulationId).value
    val logger = streams.value.log

    val simulationStartResult = enterpriseSimulationStartResult(config).evaluated

    if (simulationStartResult.createdSimulation) {
      logCreatedSimulation(logger, simulationStartResult.simulation)
    }

    if (settingSimulationId.isEmpty) {
      logSimulationConfiguration(logger, config, simulationStartResult.simulation.id)
    }

    val reportsUrl = baseUrl.toExternalForm + simulationStartResult.runSummary.reportsPath
    logger.success(s"Simulation successfully started; once running, reports will be available at $reportsUrl")
  }

  def settings(config: Configuration) = Seq(
    config / enterpriseUrl := new URL("https://cloud.gatling.io"),
    config / enterprisePackage := buildEnterprisePackage(config).value,
    config / enterpriseUpload := uploadEnterprisePackage(config).value,
    config / enterpriseStart := enterpriseSimulationStart(config).evaluated,
    config / enterprisePackageId := sys.props.get("gatling.enterprise.packageId").getOrElse(""),
    config / enterpriseTeamId := sys.props.get("gatling.enterprise.teamId").getOrElse(""),
    config / enterpriseSimulationId := sys.props.get("gatling.enterprise.simulationId").getOrElse(""),
    config / enterpriseSimulationSystemProperties := Map.empty,
    config / enterpriseApiToken := sys.props.get("gatling.enterprise.apiToken").orElse(sys.env.get("GATLING_ENTERPRISE_API_TOKEN")).getOrElse(""),
    config / packageBin := (config / enterprisePackage).value, // If we directly use config / enterprisePackage for publishing, classifiers (-tests or -it) are not correctly handled.
    config / enterpriseSimulationClass := sys.props.get("gatling.enterprise.simulationClass").getOrElse("")
  )

  private def legacyAssemblySetting(config: Configuration) =
    config / assembly := legacyPackageEnterpriseJar(config).value

  private def breakIfLegacyPluginFoundSetting =
    Global / onLoad := onLoadBreakIfLegacyPluginFound.value

  lazy val projectSettings: Seq[Def.Setting[_]] =
    Seq(legacyAssemblySetting(Test), legacyAssemblySetting(IntegrationTest), breakIfLegacyPluginFoundSetting)
}
