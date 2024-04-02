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

package io.gatling.sbt.settings.gatling

import scala.util.{ Failure, Try }

import io.gatling.plugin.exceptions._

import sbt.Configuration
import sbt.internal.util.ManagedLogger

class RecoverEnterprisePluginException(config: Configuration) {
  protected def recoverEnterprisePluginException[U](logger: ManagedLogger): PartialFunction[Throwable, Try[U]] = {
    case e: UnsupportedJavaVersionException =>
      logger.error(
        s"""${e.getMessage}
           |In order to target the supported Java version, please use the following sbt settings:
           |scalacOptions ++= Seq("-target:${e.supportedVersion}", "-release", "${e.supportedVersion}")
           |javacOptions ++= Seq("--release", "${e.supportedVersion}")
           |See also the Scala compiler documentation: https://docs.scala-lang.org/overviews/compiler-options/index.html , and the Java compiler documentation: https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html#compiling-for-earlier-releases-of-the-platform
           |Alternatively, the reported class may come from your project's dependencies, published targeting Java ${e.version}. In this case you need to use dependencies which target Java ${e.supportedVersion} or lower.
           |""".stripMargin
      )
      Failure(ErrorAlreadyLoggedException)
    case e: UserQuitException =>
      logger.warn(e.getMessage)
      Failure(ErrorAlreadyLoggedException)
  }

  protected def logSimulationConfiguration(logger: ManagedLogger, waitForRunEndSetting: Boolean): Unit =
    if (!waitForRunEndSetting) {
      logger.info(
        s"""To wait for the end of the run when starting a simulation on Gatling Enterprise, specify -Dgatling.enterprise.waitForRunEnd=true, or add the configuration to your SBT settings, e.g.:
           |${config.id} / waitForRunEnd := true
           |""".stripMargin
      )
    }
}
