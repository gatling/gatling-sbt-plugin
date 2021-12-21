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

package io.gatling.sbt

import sbt._

/** List of SBT keys for Gatling specific tasks. */
object GatlingKeys {

  // Configurations
  val Gatling = config("gatling") extend Test
  val GatlingIt = config("gatling-it") extend IntegrationTest

  // OSS Tasks
  val startRecorder = inputKey[Unit]("Start Gatling's Recorder")
  val lastReport = inputKey[Unit]("Open last Gatling report in browser")
  val copyConfigFiles = taskKey[Set[File]]("Copy Gatling's config files if missing")
  val copyLogbackXml = taskKey[File]("Copy Gatling's default logback.xml if missing")
  val generateReport = inputKey[Unit]("Generate report for a specific simulation")

  // Enterprise Settings
  val enterpriseUrl = settingKey[URL]("Target URL on Gatling Enterprise")
  val enterpriseApiToken = settingKey[String]("API Token for package upload on Gatling Enterprise")
  val enterprisePackageId = settingKey[String]("Target package ID on Gatling Enterprise")
  val enterpriseDefaultSimulationClassname = settingKey[String]("Target simulation class name on Gatling Enterprise")
  val enterpriseDefaultSimulationTeamId = settingKey[String]("Target team ID on Gatling Enterprise")
  val enterpriseSimulationId = settingKey[String]("Target simulation ID on Gatling Enterprise")
  val enterpriseSimulationSystemProperties = settingKey[Map[String, String]]("Simulation system properties on Gatling Enterprise")

  // Enterprise Tasks
  val enterprisePackage = taskKey[File]("Build a package for Gatling Enterprise")
  val enterpriseUpload = taskKey[Unit]("Upload a package for Gatling Enterprise")
  val enterpriseStart = taskKey[Unit]("Start a simulation for Gatling Enterprise")
  val assembly = taskKey[File](
    "Builds a package for Gatling Enterprise (deprecated, please use 'Gatling / enterprisePackage' or 'GatlingIt / enterprisePackage' instead)."
  )
}
