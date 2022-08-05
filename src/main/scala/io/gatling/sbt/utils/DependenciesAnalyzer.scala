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

package io.gatling.sbt.utils
import java.io.File

import scala.annotation.tailrec

import sbt.librarymanagement._
import sbt.util.Logger

object ArtifactWithoutVersion {
  def apply(moduleId: ModuleID): ArtifactWithoutVersion =
    ArtifactWithoutVersion(moduleId.organization, moduleId.name)
}

case class ArtifactWithoutVersion(organization: String, name: String)

case class DependenciesAnalysisResult(gatlingVersion: String, nonGatlingDependencies: Vector[File])

object DependenciesAnalyzer {

  private final case class Exclusion(organization: String, name: Option[String] = None)
  private object Exclusion {
    val All = Seq(
      Exclusion("io.gatling"),
      Exclusion("io.gatling.highcharts"),
      Exclusion("io.gatling.frontline"),
      // having multiple slf4-j back-ends on the classpath is an issue
      Exclusion("ch.qos.logback"),
      // scala-library and scala-reflect are always direct dependencies
      Exclusion("org.scala-lang", Some("scala-library")),
      Exclusion("org.scala-lang", Some("scala-reflect")),
      Exclusion("io.netty", Some("netty-all")),
      Exclusion("io.netty", Some("netty-resolver-dns-classes-macos")),
      Exclusion("io.netty", Some("netty-resolver-dns-native-macos"))
    )
  }

  private def exclude(dep: ArtifactWithoutVersion): Boolean =
    Exclusion.All.exists(exclusion => exclusion.organization == dep.organization && exclusion.name.forall(_ == dep.name))

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
      .getOrElse(throw new IllegalStateException("Cannot build a fatjar with unresolved dependencies"))

    val configurationReport = updateReport
      .configuration(ConfigRef.configToConfigRef(config))
      .getOrElse(throw new IllegalStateException(s"Could not find a report for configuration $config"))

    val gatlingVersion = configurationReport.modules
      .map(_.module)
      .collectFirst { case module if module.organization == "io.gatling" && module.name.startsWith("gatling-app") => module.revision }
      .getOrElse(throw new IllegalArgumentException("Couldn't locate Gatling libraries in the classpath"))

    val callers = moduleCallers(configurationReport.modules)
    val excludedDeps = configurationReport.modules.filterNot(isTransitiveGatlingDependency(_, callers))

    val nonGatlingDependencies = excludedDeps
      .flatMap(_.artifacts)
      .collect { case (artifact, file) if artifact.`type` == Artifact.DefaultType || artifact.`type` == "bundle" => file }

    DependenciesAnalysisResult(gatlingVersion, nonGatlingDependencies)
  }

  private def moduleCallers(reports: Vector[ModuleReport]): Map[ArtifactWithoutVersion, List[ArtifactWithoutVersion]] =
    reports
      .map(report => ArtifactWithoutVersion(report.module) -> report.callers.map(caller => ArtifactWithoutVersion(caller.caller)))
      .groupBy(_._1) // sadly Caller misses classifier, see https://github.com/sbt/sbt/issues/5491, so we merge modules
      .mapValues(_.flatMap(_._2.toSet).toList)
      .toVector // because mapValue is a view
      .toMap

  private def isTransitiveGatlingDependency(report: ModuleReport, callers: Map[ArtifactWithoutVersion, List[ArtifactWithoutVersion]]): Boolean = {
    @tailrec
    def isTransitiveGatlingDependencyRec(toCheck: List[ArtifactWithoutVersion]): Boolean =
      toCheck match {
        case Nil                      => false
        case dep :: _ if exclude(dep) => true
        case dep :: rest              => isTransitiveGatlingDependencyRec(callers.getOrElse(dep, Nil) ::: rest)
      }

    isTransitiveGatlingDependencyRec(List(ArtifactWithoutVersion(report.module.withConfigurations(None))))
  }
}
