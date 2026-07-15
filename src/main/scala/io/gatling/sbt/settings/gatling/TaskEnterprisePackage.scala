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
      case x =>
        logger.error(s"gatling-sbt expected a ModuleDescriptorConfiguration, but got a ${x.getClass}")
        throw ErrorAlreadyLoggedException
    }
  }

  val buildEnterprisePackage: InitializeTask[File] = Def.task {
    val moduleDescriptor = moduleDescriptorConfig.value

    val DependenciesAnalysisResult(gatlingDependencies, nonGatlingDependencies) = DependenciesAnalyzer.analyze(
      dependencyResolution.value,
      updateConfiguration.value,
      (update / unresolvedWarningConfiguration).value,
      config,
      streams.value.log,
      moduleDescriptor
    )

    // The compiled simulations live in the project's own class directories. On sbt 1.x these appear in the
    // classpath as directories; on sbt 2.x the classpath instead carries packaged jars, so we get the class
    // directories from `classDirectory`. Evaluating `fullClasspath` triggers compilation on both versions.
    val classesDirectories = Compat.classesDirectories(
      (config / fullClasspath).value,
      Seq((config / classDirectory).value, (Compile / classDirectory).value),
      fileConverter.value
    )

    val pluginLogger = EnterprisePluginIO.enterprisePluginLoggerTask.value

    val rootModule = moduleDescriptor.module

    // Internal (inter-project) dependencies exported as jars are packaged as extra library jars, like the
    // Maven and Gradle plugins do (`DependenciesAnalyzer` only sees external modules, and on sbt 2.x internal
    // dependencies no longer appear on the classpath as class directories). The project's own products are
    // excluded: their classes are already packaged through `classesDirectories`.
    val internalDependencies = Compat
      .internalDependencies((config / internalDependencyClasspath).value, fileConverter.value)
      .collect {
        case (moduleId, jar) if moduleId.organization != rootModule.organization || moduleId.name != rootModule.name =>
          new Dependency(
            new Dependency.Id(moduleId.organization, moduleId.name, moduleId.revision, null),
            jar
          )
      }
      .toSet

    target.value.mkdirs()
    val packageFile = target.value / s"${moduleName.value}-gatling-enterprise-${version.value}.jar"

    new EnterprisePackager(pluginLogger)
      .createEnterprisePackage(
        classesDirectories.asJava,
        gatlingDependencies.asJava,
        (nonGatlingDependencies ++ internalDependencies).asJava,
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
