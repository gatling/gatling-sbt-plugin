package gatling.sbt

import org.scalatools.testing._

import io.gatling.core.Predef._
//import io.gatling.core.result.message.RunRecord
import io.gatling.core.runner.Runner
import io.gatling.charts.report.ReportsGenerator
import io.gatling.charts.config.ChartsFiles._

import org.joda.time.DateTime._


import GatlingFingerprints._

class GatlingFramework extends Framework {
  
  GatlingBootstrap(
    sys.props.get("sbt.gatling.conf.file").get, 
    sys.props.get("sbt.gatling.result.dir").get
  )

  def name = "gatling"

  def tests = Array[Fingerprint](simulationClass, simulationModule)

  def testRunner(testClassLoader:ClassLoader, loggers:Array[Logger]) = new TestInterfaceGatling(testClassLoader, loggers)
}

trait GatlingSimulationFingerprint extends TestFingerprint {
  def superClassName = "gatling.sbt.PerfTest"
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
    gatling(createInstanceFor[PerfTest](loadClassOf(className, loader)), handler)



  private def createInstanceFor[T <: AnyRef](klass: Class[T])(implicit m: ClassManifest[T]) = {
      val constructor = klass.getDeclaredConstructors()(0)
      constructor.setAccessible(true)
      try {
          val instance: AnyRef = constructor.newInstance().asInstanceOf[AnyRef]
          if (!m.erasure.isInstance(instance)) {
            error(instance + " is not an instance of " + m.erasure.getName)
          }
          instance.asInstanceOf[T]
      } catch {
          case e: java.lang.reflect.InvocationTargetException => {
            loggers.foreach(_.error(s"Unable to create test instance: ${e.getMessage}"))
            throw e
          }
      }
  }
  
  private def loadClassOf[T <: AnyRef](className: String = "", loader: ClassLoader = Thread.currentThread.getContextClassLoader): Class[T] = 
      loader.loadClass(className).asInstanceOf[Class[T]]


  private def createEvent = new Event {
    val testName = "run-test"

    val description = "stress-test"

    val result = Result.Success

    val error = null
  }

  def gatling(s: PerfTest, handler:EventHandler) {
    println("Creating run record")
    //val runInfo = new RunRecord(now, "run-test", "stress-test")
    //println("Run record created > run scenario")

    s.exec(/*runInfo*/)

    //println("Simulation Finished.")
    //runInfo.runUuid

    println("scenarion ran > generate reports")
    generateReports(/*runInfo.runUuid*/)
    println("reports generated")

    handler.handle(createEvent)
  }

  def generateReports(runUuid: String = "<none>") {
    //println("Generating reports...")
    //val start = System.currentTimeMillis
    //ReportsGenerator.generateFor(runUuid)
    //println("Reports generated in " + (System.currentTimeMillis - start) / 1000 + "s.")

    println("TODO: api changed :/")
  }

}