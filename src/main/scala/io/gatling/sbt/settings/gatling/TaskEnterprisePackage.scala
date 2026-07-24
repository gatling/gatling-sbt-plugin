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

import java.io.File

import scala.collection.JavaConverters._

import io.gatling.plugin.pkg.{ Dependency, EnterprisePackager }
import io.gatling.sbt.Compat
import io.gatling.sbt.settings.gatling.EnterpriseUtils.InitializeTask
import io.gatling.sbt.utils._

import sbt.{ Configuration, Def, ModuleDescriptorConfiguration, Test, _ }
import sbt.Keys._

object TaskEnterprisePackage {
  private val SbtPackagerName = "sbt"
}

class TaskEnterprisePackage(config: Configuration) {
  private val moduleDescriptorConfig = Def.task {
    val logger = streams.value.log
    moduleSettings.value match {
      case config: ModuleDescriptorConfiguration => config
      case x                                     =>
        logger.error(s"gatling-sbt expected a ModuleDescriptorConfiguration, but got a ${x.getClass}")
        throw ErrorAlreadyLoggedException
    }
  }

  val buildEnterprisePackage: InitializeTask[File] = Def.task {
    val moduleDescriptor = moduleDescriptorConfig.value

    // The classpath (`fullclasspath`) consists of:
    // - `exportedProducts` (1), classes and resources from this project
    // - `dependencyClasspath`, itself consisting of:
    //   - `internalDependencyClasspath` (2), from other projects in the same build
    //   - `externalDependencyClasspath` (3), external libraries

    // (1) `exportedProducts` from this specific project/configuration (i.e. classes and resources in "test" in this project)
    val allProjectEntries = Compat.toFiles((config / exportedProducts).value, fileConverter.value)

    // (2) `internalDependencyClasspath`, this includes classes/resources from:
    // - other configurations this one depends on in the same project (normally the `Compile` configuration, i.e. classes and resources in "main" in this project)
    // - other projects this one depends on using `.dependsOn()`
    val allInternalDependencies = Compat.toFiles((config / internalDependencyClasspath).value, fileConverter.value)

    // (1) and (2)
    // On sbt 1.x they are directories (e.g. <my-project>/target/scala-2.13/test-classes/).
    // On sbt 2.x they are JARs (e.g. target/out/jvm/scala-2.13.18/<my-project>/<my-project>_2.13-<version>>-tests.jar).
    // For simplicity and consistency, we simply handle both directories and JARs either way.
    val allInternalEntries = allInternalDependencies ++ allProjectEntries
    val classesDirectories = allInternalEntries.filter(_.isDirectory)
    val jarFiles = allInternalEntries.filter(_.isFile)

    // (3) External dependencies (from `.libraryDependencies()`)  are collected with the DependenciesAnalyzer
    val DependenciesAnalysisResult(gatlingDependencies, nonGatlingDependencies) = DependenciesAnalyzer.analyze(
      dependencyResolution.value,
      updateConfiguration.value,
      (update / unresolvedWarningConfiguration).value,
      config,
      streams.value.log,
      moduleDescriptor
    )

    target.value.mkdirs()
    val packageFile = target.value / s"${moduleName.value}-gatling-enterprise-${version.value}.jar"

    val pluginLogger = EnterprisePluginIO.enterprisePluginLoggerTask.value
    val rootModule = moduleDescriptor.module
    new EnterprisePackager(pluginLogger)
      .createEnterprisePackage(
        classesDirectories.asJava,
        jarFiles.asJava,
        gatlingDependencies.asJava,
        nonGatlingDependencies.asJava,
        rootModule.organization,
        rootModule.name,
        rootModule.revision,
        TaskEnterprisePackage.SbtPackagerName,
        getClass.getPackage.getImplementationVersion,
        packageFile,
        baseDirectory.value
      )

    packageFile
  }

  val legacyPackageEnterpriseJar: InitializeTask[File] = Def.sequential(
    Def.task {
      val newCommand = config.id match {
        case Test.id                   => "Gatling / enterprisePackage"
        case Compat.IntegrationTest.id => "GatlingIt / enterprisePackage"
        case _                         => "Gatling / enterprisePackage or GatlingIt / enterprisePackage"
      }
      streams.value.log.warn(
        s"""Task ${config.id} / assembly is deprecated and will be removed in a future version.
           |Please use $newCommand instead.
           |See https://docs.gatling.io/reference/integrations/build-tools/sbt-plugin/ for more information.""".stripMargin
      )
    },
    buildEnterprisePackage
  )
}
