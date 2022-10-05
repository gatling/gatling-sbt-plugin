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

package io.gatling.sbt.settings

import sbt._
import sbt.Keys._

object ProjectBaseSettings {
  def settings(config: Configuration): Seq[Def.Setting[_]] = Seq(
    artifacts := {
      if ((config / publishArtifact).value)
        artifacts.value :+ (config / packageBin / artifact).value
      else
        artifacts.value
    },
    packagedArtifacts := {
      if ((config / publishArtifact).value)
        packagedArtifacts.value.updated((config / packageBin / artifact).value, (config / packageBin).value)
      else
        packagedArtifacts.value
    }
  )
}
