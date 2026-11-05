import java.util.jar.JarFile

ThisBuild / scalaVersion := "2.13.18"
ThisBuild / version := "1.2.3"

val gatlingVersion = "3.15.0"

val checkPackage = taskKey[Unit]("Checks that the enterprise package contains the classes of this module and of its internal dependencies")

lazy val lib = project

lazy val loadtest = project
  .enablePlugins(GatlingPlugin)
  .dependsOn(lib)
  .settings(
    libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % Test,
    libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingVersion % Test,
    checkPackage := {
      val jar = (Gatling / enterprisePackage).value
      val jarFile = new JarFile(jar)
      val entries =
        try {
          val builder = Set.newBuilder[String]
          val en = jarFile.entries()
          while (en.hasMoreElements) builder += en.nextElement().getName
          builder.result()
        } finally jarFile.close()
      def checkContains(entry: String): Unit =
        if (!entries.contains(entry)) sys.error(s"Missing entry $entry in enterprise package $jar")
      // Simulation classes from this module
      checkContains("basic/MultiModuleSimulation.class")
      // Classes from the internal `lib` module dependency
      checkContains("lib/SimulationConfig.class")
    }
  )

// Reports folder of `loadtest`, resolved explicitly as the root project (which doesn't enable the plugin) is the current one
def loadtestReportsFolder(state: State): File =
  Project.extract(state).get(LocalProject("loadtest") / Gatling / target)

def loadtestReports(state: State): Seq[File] =
  Option(loadtestReportsFolder(state).listFiles()).toSeq.flatten.filter(dir => (dir / "simulation.log").isFile)

// Deletes the HTML of the generated reports, keeping the simulation results so that they can be regenerated
commands += Command.command("deleteLoadtestReportsHtml") { state =>
  loadtestReports(state).foreach(dir => IO.delete(dir / "index.html"))
  state
}

// Runs `loadtest/Gatling/generateReport <report name>` from the root project, so that the report name is parsed and
// validated against the reports of `loadtest`, and not of the current project
commands += Command.command("generateLoadtestReportByName") { state =>
  loadtestReports(state) match {
    case Seq(report) => s"loadtest/Gatling/generateReport ${report.getName}" :: state
    case reports     => sys.error(s"Expected exactly one report, got: ${reports.mkString(", ")}")
  }
}
