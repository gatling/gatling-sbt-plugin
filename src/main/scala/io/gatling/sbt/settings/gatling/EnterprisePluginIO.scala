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

package io.gatling.sbt.settings.gatling

import io.gatling.plugin.io._

import sbt.Def
import sbt.Keys._

object EnterprisePluginIO {
  val enterprisePluginLoggerTask = Def.task {
    val logger = streams.value.log
    new PluginLogger {
      override def info(message: String): Unit = logger.info(message)
      override def error(message: String): Unit = logger.error(message)
    }
  }

  val enterprisePluginScannerTask = Def.task {
    val interactiveService = interactionService.value
    new PluginScanner {
      override def readString(): String = interactiveService.readLine("> ", mask = false).getOrElse("")
      override def readInt(): Int = Integer.parseInt(readString())
    }
  }

  val enterprisePluginIOTask = Def.task {
    val logger = enterprisePluginLoggerTask.value
    val scanner = enterprisePluginScannerTask.value
    new PluginIO {
      override def getLogger: PluginLogger = logger
      override def getScanner: PluginScanner = scanner
    }
  }
}
