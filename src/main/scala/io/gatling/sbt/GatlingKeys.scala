/*
 * Copyright 2011-2021 GatlingCorp (https://gatling.io)
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

import _root_.io.gatling.sbt.utils.PropertyUtils.DefaultJvmArgs

import sbt._

/** List of SBT keys for Gatling specific tasks. */
object GatlingKeys {

  // ----------- //
  // -- Tasks -- //
  // ----------- //

  val assembly = taskKey[File]("Builds a fatjar for FrontLine.")
  val startRecorder = inputKey[Unit]("Start Gatling's Recorder")
  val lastReport = inputKey[Unit]("Open last Gatling report in browser")
  val copyConfigFiles = taskKey[Set[File]]("Copy Gatling's config files if missing")
  val copyLogbackXml = taskKey[File]("Copy Gatling's default logback.xml if missing")
  val generateReport = inputKey[Unit]("Generate report for a specific simulation")

  // -------------------- //
  // -- Configurations -- //
  // -------------------- //

  val Gatling = config("gatling") extend Test
  val GatlingIt = config("gatling-it") extend IntegrationTest

  // -------------------- //
  // -- Helper methods -- //
  // -------------------- //

  private val unPropagatedPropertiesRoots =
    List("java.", "sun.", "jline.", "file.", "awt.", "os.", "user.")

  def overrideDefaultJavaOptions(javaOptions: String*): Seq[String] =
    propagatedSystemProperties ++ DefaultJvmArgs ++ javaOptions

  private def isPropagatedSystemProperty(name: String) =
    !(unPropagatedPropertiesRoots.exists(name.startsWith) ||
      name == "line.separator" ||
      name == "path.separator" ||
      name == "gopherProxySet")

  private def property(key: String, value: String) = s"-D$key=$value"

  private def propagatedSystemProperties: Seq[String] =
    sys.props
      .filterKeys(isPropagatedSystemProperty)
      .map { case (key, value) => property(key, value) }
      .toSeq

}
