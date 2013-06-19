package gatling.sbt

import io.gatling.core.config.GatlingConfiguration
import io.gatling.app.CommandLineConstants ._

import scala.collection.mutable

case class GatlingBootstrap(gatlingFile:String, resultsFolder:String) {

  // took back from  https://github.com/excilys/gatling/blob/1.5.X/gatling-maven-plugin/src/main/java/com/excilys/ebi/gatling/mojo/GatlingMojo.java
  /*public static final String[] JVM_ARGS = new String[] { "-server", "-XX:+UseThreadPriorities", "-XX:ThreadPriorityPolicy=42", "-Xms512M", "-Xmx512M", "-Xmn100M", "-Xss2M",
        "-XX:+HeapDumpOnOutOfMemoryError", "-XX:+AggressiveOpts", "-XX:+OptimizeStringConcat", "-XX:+UseFastAccessorMethods", "-XX:+UseParNewGC", "-XX:+UseConcMarkSweepGC",
        "-XX:+CMSParallelRemarkEnabled", "-XX:+CMSClassUnloadingEnabled", "-XX:CMSInitiatingOccupancyFraction=75", "-XX:+UseCMSInitiatingOccupancyOnly", "-XX:SurvivorRatio=8",
        "-XX:MaxTenuringThreshold=1" };
  */

  val gatlingConfiguration = GatlingConfiguration.setUp(mutable.Map[String,Any](
    // gatlingFile
    CLI_DATA_FOLDER -> sys.props("java.io.tmpdir"), //todo dataFolder.getAbsolutePath
    CLI_REQUEST_BODIES_FOLDER -> sys.props("java.io.tmpdir"), //todo requestBodiesFolder.getAbsolutePath
    CLI_RESULTS_FOLDER -> resultsFolder,
    CLI_SIMULATIONS_FOLDER -> sys.props("java.io.tmpdir")//todo simulationSourcesFolder.getAbsolutePath
    )
  )

}