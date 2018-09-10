/**
 * Copyright 2011-2018 GatlingCorp (http://gatling.io)
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

object PropertyUtils {

  val DefaultJvmArgs = List(
    "-server", "-Xmx1G",
    "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=30", "-XX:G1HeapRegionSize=16m", "-XX:InitiatingHeapOccupancyPercent=75",
    "-XX:+ParallelRefProcEnabled", "-XX:+PerfDisableSharedMem",
    "-XX:+AggressiveOpts", "-XX:+OptimizeStringConcat",
    "-XX:+HeapDumpOnOutOfMemoryError",
    "-Djava.net.preferIPv4Stack=true", "-Djava.net.preferIPv6Addresses=false"
  )
}
