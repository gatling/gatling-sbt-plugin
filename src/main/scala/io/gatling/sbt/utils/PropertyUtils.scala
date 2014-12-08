package io.gatling.sbt.utils

object PropertyUtils {

  val DefaultJvmArgs = List(
    "-server", "-XX:+UseThreadPriorities", "-XX:ThreadPriorityPolicy=42", "-Xms512M",
    "-Xmx512M", "-Xmn100M", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:+AggressiveOpts",
    "-XX:+OptimizeStringConcat", "-XX:+UseFastAccessorMethods", "-XX:+UseParNewGC",
    "-XX:+UseConcMarkSweepGC", "-XX:+CMSParallelRemarkEnabled")

  private val unPropagatedPropertiesRoots =
    List("java.", "sun.", "jline.", "file.", "awt.", "os.", "user.")

  private def isPropagatedSystemProperty(name: String) =
    !(unPropagatedPropertiesRoots.exists(name.startsWith) ||
      name == "line.separator" ||
      name == "path.separator" ||
      name == "gopherProxySet")

  private def property(key: String, value: String) = s"-D$key=$value"

  def propagatedSystemProperties: Seq[String] =
    sys.props
      .filterKeys(isPropagatedSystemProperty)
      .map { case (key, value) => property(key, value) }
      .toSeq
}
