package gatling.sbt

import java.lang.System.currentTimeMillis

import org.scalatools.testing._

import io.gatling.core.Predef._
import io.gatling.core.config.GatlingFiles
import io.gatling.core.runner.Selection
import io.gatling.core.result.reader.DataReader

import io.gatling.charts.report.ReportsGenerator
import io.gatling.charts.config.ChartsFiles._

import org.joda.time.DateTime._


import GatlingFingerprints._

class GatlingFramework extends Framework {

  GatlingBootstrap(
    sys.props.get("sbt.gatling.conf.file").getOrElse("gatling.conf"),
    sys.props.get("sbt.gatling.result.dir").getOrElse("target/results")
  )

  def name = "gatling"

  def tests = Array[Fingerprint](simulationClass, simulationModule)

  def testRunner(testClassLoader:ClassLoader, loggers:Array[Logger]) = new TestInterfaceGatling(testClassLoader, loggers)
}

trait GatlingSimulationFingerprint extends TestFingerprint {
  def superClassName = "gatling.sbt.PerfTest" // it extends Simulation
}

object GatlingFingerprints {

  val simulationClass  = new GatlingSimulationFingerprint(){def isModule = false}
  val simulationModule = new GatlingSimulationFingerprint(){def isModule = true}

}

class TestInterfaceGatling(loader: ClassLoader, val loggers: Array[Logger]) extends org.scalatools.testing.Runner {
  def run(className: String, fingerprint: TestFingerprint, handler: EventHandler, args: Array[String]) = {
    runTest(className, fingerprint, handler, args)
  }


  def runTest(className: String, fingerprint: TestFingerprint, handler: EventHandler, args: Array[String]) =
    if (fingerprint.superClassName == simulationClass.superClassName) {
      runSimulation(className, handler, args)
    } else {
      loggers.foreach(_.warn(s"Skipped test: $fingerprint"))
    }

  def runSimulation(className:String,  handler: EventHandler, args: Array[String]) =
    gatling(loadClassOf[PerfTest](className, loader), handler) // // PerfTest extends Simulation

  private def loadClassOf[T <: AnyRef](className: String = "", loader: ClassLoader = Thread.currentThread.getContextClassLoader): Class[T] =
      loader.loadClass(className).asInstanceOf[Class[T]]


  private def createEvent = new Event {
    val testName = "run-test"

    val description = "stress-test"

    val result = Result.Success

    val error = null
  }

  def gatling(s: Class[PerfTest], handler:EventHandler) {
    val simulation = s.newInstance

    val runner = new AdaptedRunner[PerfTest](simulation)

    simulation.pre
    val (runId, sim) = runner.run
    simulation.post

    println(s"Simulation Finished: $runId")

    println("scenarios ran > generate reports")
    generateReports(runId)
    println("reports generated")

    handler.handle(createEvent)
  }

  def generateReports(runId: String = "<none>") {
    val outputDirectory = GatlingFiles.reportsOnlyDirectory
                                          .map(_ + "/" + runId)
                                          .getOrElse(
                                            throw new RuntimeException("Temp exception : due to missing CONF_CORE_DIRECTORY_REPORTS_ONLY value")
                                          )

    lazy val dataReader = DataReader.newInstance(runId)

    println("Generating reports...")
    val start = currentTimeMillis

    val indexFile = ReportsGenerator.generateFor(outputDirectory, dataReader)

    println(s"Reports generated in ${(currentTimeMillis - start) / 1000}s.")
    println(s"Please open the following file : ${indexFile.toAbsolute}")
  }

}