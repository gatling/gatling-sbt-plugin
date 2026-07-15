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
