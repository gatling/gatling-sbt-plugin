/*
 * Copyright 2011-2024 GatlingCorp (https://gatling.io)
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

import io.gatling.sbt.settings._

import sbt._
import sbt.Keys._

object GatlingPlugin extends AutoPlugin {
  // AutoPlugin setup
  override val requires = plugins.JvmPlugin
  val autoImport: GatlingKeys.type = GatlingKeys
  import autoImport._

  override def projectConfigurations: Seq[Configuration] = Seq(Gatling, GatlingIt, IntegrationTest)
  override def projectSettings: Seq[Def.Setting[_]] = gatlingSettings ++ gatlingItSettings ++ ProjectSettings.projectSettings

  // Test framework definition
  val gatlingTestFramework = TestFramework("io.gatling.sbt.GatlingFramework")

  // Settings
  lazy val gatlingSettings: Seq[Def.Setting[_]] =
    inConfig(Gatling)(
      Defaults.testTasks ++
        (forkOptions := Defaults.forkOptionsTask.value) ++
        BaseSettings.settings(Gatling, Test)
    ) ++ ProjectBaseSettings.settings(Gatling)

  lazy val gatlingItSettings: Seq[Def.Setting[_]] =
    inConfig(GatlingIt)(
      Defaults.itSettings ++
        Defaults.testTasks ++
        (forkOptions := Defaults.forkOptionsTask.value) ++
        BaseSettings.settings(GatlingIt, IntegrationTest)
    ) ++ ProjectBaseSettings.settings(GatlingIt)
}
