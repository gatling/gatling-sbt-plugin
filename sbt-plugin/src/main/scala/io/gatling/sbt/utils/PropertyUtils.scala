package io.gatling.sbt.utils

object PropertyUtils {

  val DefaultJvmArgs = List(
    "-server", "-XX:+UseThreadPriorities", "-XX:ThreadPriorityPolicy=42", "-Xms512M",
    "-Xmx512M", "-Xmn100M", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:+AggressiveOpts",
    "-XX:+OptimizeStringConcat", "-XX:+UseFastAccessorMethods", "-XX:+UseParNewGC",
    "-XX:+UseConcMarkSweepGC", "-XX:+CMSParallelRemarkEnabled")
}
