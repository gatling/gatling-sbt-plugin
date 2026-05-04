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

package io.gatling.sbt.utils

import scala.collection.mutable

import io.gatling.plugin.pkg.Dependency

import sbt.internal.graph.backend.SbtUpdateReport
import sbt.librarymanagement._
import sbt.util.Logger

case class DependenciesAnalysisResult(gatlingDependencies: Set[Dependency], extraDependencies: Set[Dependency])

object DependenciesAnalyzer {

  private case class ModuleWithoutVersion(organization: String, name: String)

  def analyze(
      resolution: DependencyResolution,
      updateConfig: UpdateConfiguration,
      unresolvedWarningConfig: UnresolvedWarningConfiguration,
      config: Configuration,
      logger: Logger,
      rootModule: ModuleDescriptorConfiguration
  ): DependenciesAnalysisResult = {
    val moduleDescriptor = resolution.moduleDescriptor(rootModule)

    val updateReport = resolution
      .update(moduleDescriptor, updateConfig, unresolvedWarningConfig, logger)
      .getOrElse(throw new IllegalStateException("Cannot build a package with unresolved dependencies"))

    val configurationReport = updateReport
      .configuration(ConfigRef.configToConfigRef(config))
      .getOrElse(throw new IllegalStateException(s"Could not find a report for configuration $config"))

    val allArtifacts = for {
      modReport <- configurationReport.modules
      (artifact, file) <- modReport.artifacts if artifact.`type` == "jar" || artifact.extension == "jar"
    } yield (ModuleWithoutVersion(modReport.module.organization, modReport.module.name), artifact, file)

    val dependencyMap = SbtUpdateReport.fromConfigurationReport(configurationReport, rootModule.module).dependencyMap

    val moduleGraphWithoutVersions: Map[ModuleWithoutVersion, Set[ModuleWithoutVersion]] =
      for {
        (module, children) <- dependencyMap
      } yield ModuleWithoutVersion(module.organization, module.name) -> children.map(child => ModuleWithoutVersion(child.id.organization, child.id.name)).toSet

    val allModules = moduleGraphWithoutVersions.keySet ++ moduleGraphWithoutVersions.values.flatten
    val gatlingModules = allModules.filter(module => module.organization == "io.gatling" || module.organization == "io.gatling.highcharts")
    val gatlingGraphModules = collectDepAndChildren(gatlingModules, moduleGraphWithoutVersions)

    val extraModules = allModules -- gatlingGraphModules - ModuleWithoutVersion(rootModule.module.organization, rootModule.module.name)

    val moduleToDependencies: Map[ModuleWithoutVersion, Seq[Dependency]] =
      (for {
        graphModule <- dependencyMap.values.flatten
        moduleWithoutVersion = ModuleWithoutVersion(graphModule.id.organization, graphModule.id.name)
      } yield {
        val dependencies = for {
          (_, artifact, jarFile) <- allArtifacts.filter(_._1 == moduleWithoutVersion)
        } yield new Dependency(
          new Dependency.Id(
            graphModule.id.organization,
            graphModule.id.name,
            graphModule.id.version,
            artifact.classifier.orNull
          ),
          jarFile
        )
        moduleWithoutVersion -> dependencies
      }).toMap

    DependenciesAnalysisResult(
      gatlingModules.flatMap(moduleToDependencies.getOrElse(_, List.empty)),
      extraModules.flatMap(moduleToDependencies.getOrElse(_, List.empty))
    )
  }

  private def collectDepAndChildren(
      gatlingModules: Set[ModuleWithoutVersion],
      moduleGraphWithoutVersions: Map[ModuleWithoutVersion, Set[ModuleWithoutVersion]]
  ): Set[ModuleWithoutVersion] = {
    def collectDepAndChildren(module: ModuleWithoutVersion, deps: mutable.Set[ModuleWithoutVersion]): Unit =
      if (!deps.contains(module)) {
        deps.add(module)
        for {
          children <- moduleGraphWithoutVersions.getOrElse(module, Set.empty)
        } collectDepAndChildren(children, deps)
      }

    val seen = mutable.Set.empty[ModuleWithoutVersion]
    gatlingModules.foreach(collectDepAndChildren(_, seen))
    seen.toSet
  }
}
