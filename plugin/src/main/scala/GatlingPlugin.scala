package gatling.sbt

/**
 *
 *
 *
 * Inspired by https://github.com/bennetimo/gatling-sbt
 *
 *
 *
 */

import sbt._
import Keys._
import sbt.Tests.{SubProcess, Group}

object GatlingPlugin extends Plugin {

  //REPOs
  val gatlingReleases = "Excilys" at "http://repository.excilys.com/content/groups/public"
  //todo: remove from defaults on Gatling 2.0 Release
  val gatlingSnapshots = "Gatling Cloudbees" at "http://repository-gatling.forge.cloudbees.com/snapshot"

  //this goes from sourceGen in test-framework
  val gatlingVersion = gatling.sbt.GenConf.gatlingVersion

  //DEPENDENCIES
  val gatlingApp = "com.excilys.ebi.gatling" % "gatling-app" % gatlingVersion
  val gatlingRecorder = "com.excilys.ebi.gatling" % "gatling-recorder" % gatlingVersion
  val gatlingParent = "com.excilys.ebi.gatling" % "gatling-parent" % gatlingVersion
  val gatlingCharts = "com.excilys.ebi.gatling" % "gatling-charts" % gatlingVersion
  val gatlingHighcharts = "com.excilys.ebi.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion

  // PerfTest configuration to hold all gatling sources, under src/perf/scala
  val PerfTest = config("perf") extend (Test)

  lazy val gatlingConfFile = SettingKey[File]("gatling-conf-file", "The Gatling-Tool configuration file") in PerfTest
  lazy val gatlingResultDir = SettingKey[File]("gatling-result-dir", "The dir where Gatling-Tool will gererate results") in PerfTest

  val gatlingSettings = inConfig(PerfTest)(baseGatlingSettings)

  val gatlingTestFramework = new TestFramework("gatling.sbt.GatlingFramework")

  lazy val baseGatlingSettings = Defaults.testSettings ++ Seq(
    resolvers ++= Seq(gatlingReleases, gatlingSnapshots),
    libraryDependencies ++= gatlingDependencies ++ frameworkDependencies,
    testFrameworks := Seq(gatlingTestFramework),
    parallelExecution in PerfTest := false, //Doesn't make sense to launch multiple load tests simultaneously
    fork in PerfTest := true,
    scalaVersion in PerfTest := "2.10.2",
    testGrouping <<= definedTests in PerfTest map singleTests,

    //runRecorderTask,
    gatlingConfFile <<= baseDirectory { _ / "src" / "perf" / "resources" / "galing.conf" },
    gatlingResultDir <<= target { _ / "gatling-test" / "result" },

    testOptions in PerfTest += Tests.Setup( () => println("Setup perf tests") ),
    testOptions in PerfTest += Tests.Cleanup( () => println("Cleanup perf tests") )

    //logLevel := Level.Debug
  )

  // Group tests to be just a single test, and run each in a forked jvm.
  // This gets round the fact that the Gatling runner shutsdown the ActorSystem after the test is finished
  def singleTests(tests: Seq[TestDefinition]) =
    tests map {
      test =>
        new Group(
          name = test.name,
          tests = Seq(test),
          runPolicy = SubProcess(javaOptions = Seq.empty[String]))
    }

  val gatlingDependencies = Seq(
    gatlingApp,
    gatlingRecorder,
    gatlingCharts,
    gatlingHighcharts
  )

  val frameworkDependencies = Seq(
    "gatling" %% "gatling-sbt-test-framework" % "0.0.1-SNAPSHOT" % "perf"
  )
}
