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

import scala.annotation.nowarn

import sbt._
import xsbti.FileConverter

/**
 * sbt 1.x implementations of the few APIs that differ between sbt 1.x and sbt 2.x. The sbt 2.x counterparts live under `src/main/scala-3`, so that the rest of
 * the plugin's sources can be shared between both sbt versions.
 */
private[gatling] object Compat {

  /**
   * The built-in `IntegrationTest` configuration. It was removed in sbt 2.x, where [[Compat]] recreates an equivalent one.
   */
  @nowarn("cat=deprecation")
  val IntegrationTest: Configuration = sbt.IntegrationTest

  /**
   * Settings enabling the `IntegrationTest` (`it`) configuration, i.e. compiling and running sources found in `src/it`.
   */
  @nowarn("cat=deprecation")
  val integrationTestSettings: Seq[Def.Setting[?]] = sbt.Defaults.itSettings

  /**
   * Resolves the actual files backing a classpath. On sbt 1.x each classpath entry already carries a plain [[java.io.File]].
   */
  def toFiles(classpath: Def.Classpath, converter: FileConverter): Seq[File] =
    classpath.map(_.data)

  /**
   * The project's own compiled class directories, used to scan for simulations. On sbt 1.x they appear directly in the classpath as directories, so we keep the
   * historical behaviour of filtering the full classpath (which also captures inter-project class directories).
   */
  def classesDirectories(fullClasspath: Def.Classpath, classDirectories: Seq[File], converter: FileConverter): Seq[File] =
    toFiles(fullClasspath, converter).filter(_.isDirectory)

  /**
   * The internal (inter-project) dependencies to package as extra library jars, with their module IDs. On sbt 1.x internal dependencies usually appear on the
   * classpath as class directories and are already packaged through [[classesDirectories]], so only jar entries (`exportJars := true`) are returned here.
   */
  def internalDependencies(internalDependencyClasspath: Def.Classpath, converter: FileConverter): Seq[(ModuleID, File)] =
    internalDependencyClasspath
      .filter(_.data.isFile)
      .flatMap(entry => entry.get(Keys.moduleID.key).map(_ -> entry.data))

  /**
   * Adapts the file produced for the `packageBin` task to the value type that key expects. On sbt 1.x it is a plain [[java.io.File]].
   */
  def toPackagedArtifact(file: File, converter: FileConverter): File =
    file

  /**
   * Opts a task out of sbt 2.x's on-disk task caching, which rejects [[java.io.File]] results and requires a `JsonFormat` for other results. sbt 1.x has no
   * such caching, so this is the identity.
   */
  def uncached[A](task: A): A =
    task
}
