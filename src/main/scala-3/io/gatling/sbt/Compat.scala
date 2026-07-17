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

package io.gatling.sbt

import java.io.File

import sbt.*
import sbt.plugins.JUnitXmlReportPlugin.autoImport.testReportSettings
import xsbti.{ FileConverter, HashedVirtualFileRef }

/**
 * sbt 2.x implementations of the few APIs that differ between sbt 1.x and sbt 2.x. The sbt 1.x counterparts live under `src/main/scala-2.12`, so that the rest
 * of the plugin's sources can be shared between both sbt versions.
 */
private[gatling] object Compat {

  /**
   * The built-in `IntegrationTest` configuration was removed in sbt 2.x, so we recreate the exact one sbt 1.x used to provide: id `IntegrationTest`, Ivy name
   * `it`, extending `Runtime`. Keeping the same id and name preserves how it is referenced on the command line (`IntegrationTest / ...` and `It / ...`) and
   * where its sources (`src/it`) and reports (`target/it-reports`) live.
   */
  val IntegrationTest: Configuration = Configuration.of("IntegrationTest", "it").extend(Runtime)

  /**
   * Settings enabling the `IntegrationTest` (`it`) configuration, i.e. compiling and running sources found in `src/it`. This mirrors what sbt 1.x's
   * `Defaults.itSettings` used to provide. On sbt 2.x the built-in `JUnitXmlReportPlugin` only wires the JUnit XML report listener into `Test` (the
   * `IntegrationTest` configuration it used to also cover no longer exists), so we add `testReportSettings` here to keep generating `target/it-reports`.
   */
  val integrationTestSettings: Seq[Def.Setting[?]] =
    inConfig(IntegrationTest)(Defaults.testSettings ++ testReportSettings)

  /**
   * Resolves the actual files backing a classpath. On sbt 2.x each classpath entry carries a virtual file reference that must be resolved through the build's
   * [[xsbti.FileConverter]].
   */
  def toFiles(classpath: Def.Classpath, converter: FileConverter): Seq[File] =
    classpath.map(entry => converter.toPath(entry.data).toFile)

  /**
   * The project's compiled class directories, used to scan for simulations. On sbt 2.x the classpath usually carries packaged jars rather than class
   * directories, so we use the `classDirectory` locations directly (compilation is triggered by evaluating `fullClasspath` at the call site). Directory entries
   * still found on the classpath (`exportJars := false`) are included as well, mirroring the sbt 1.x behaviour.
   */
  def classesDirectories(fullClasspath: Def.Classpath, classDirectories: Seq[File], converter: FileConverter): Seq[File] =
    (classDirectories ++ toFiles(fullClasspath, converter)).filter(_.isDirectory).distinct

  /**
   * The internal (inter-project) dependencies to package as extra library jars, with their module IDs. On sbt 2.x internal dependencies appear on the classpath
   * as packaged jars carrying their module ID as a string attribute.
   */
  def internalDependencies(internalDependencyClasspath: Def.Classpath, converter: FileConverter): Seq[(ModuleID, File)] =
    internalDependencyClasspath
      .flatMap { entry =>
        entry.get(Keys.moduleIDStr).map { str =>
          Classpaths.moduleIdJsonKeyFormat.read(str) -> converter.toPath(entry.data).toFile
        }
      }
      .filter { case (_, file) => file.isFile }

  /**
   * Adapts the file produced for the `packageBin` task to the value type that key expects. On sbt 2.x it is a virtual file reference resolved through the
   * build's [[xsbti.FileConverter]].
   */
  def toPackagedArtifact(file: File, converter: FileConverter): HashedVirtualFileRef =
    converter.toVirtualFile(file.toPath)

  /**
   * Opts a task out of sbt 2.x's on-disk task caching, which rejects [[java.io.File]] results and requires a `JsonFormat` for other results. Delegates to sbt
   * 2.x's `Def.uncached` marker.
   */
  inline def uncached[A](inline task: A): A =
    Def.uncached(task)
}
