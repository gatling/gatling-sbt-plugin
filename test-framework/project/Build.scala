import sbt._
import Keys._

object GTFBuild extends Build {

  lazy val gatlingProject = Project("gatling-sbt", file("."), settings = gatlingTestFrameworkSettings)
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

  val gatlingVersion = "2.0.0-SNAPSHOT"

  val gatlingTestFrameworkSettings = Defaults.defaultSettings ++ Seq(
    scalaVersion := "2.10.2",
    organization := "gatling",
    name := "gatling-sbt-test-framework",
    version := "0.0.1-SNAPSHOT",
    //todo: revisit, on final versions
    publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository"))),

    resolvers ++= Seq(
      "Local Maven Repository" at file(Path.userHome.absolutePath+"/.m2/repository").toURI.toURL.toString,
      "Gatling Releases Repo" at "http://repository.excilys.com/content/repositories/releases",
      "Gatling Third-Party Repo" at "http://repository.excilys.com/content/repositories/thirdparty",
      "Gatling Cloudbees" at "http://repository-gatling.forge.cloudbees.com/snapshot"

  ),
    libraryDependencies ++= Seq(
      "org.scala-tools.testing" % "test-interface" % "0.5",
      "io.gatling" % "gatling-app" % gatlingVersion ,
      "io.gatling" % "gatling-core" % gatlingVersion ,
      "io.gatling" % "gatling-http" % gatlingVersion ,
      "io.gatling" % "gatling-recorder" % gatlingVersion ,
      "io.gatling" % "gatling-charts" % gatlingVersion ,
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion
      ),
    sourceGenerators in Compile <+= sourceManaged in Compile map { dir =>
      val file = dir / "gatling" / "sbt" / "genconf.scala"
      val contents = "val gatlingVersion=\""+gatlingVersion+"\""

      IO.write(file, "package gatling.sbt\nobject GenConf { " + contents + " }")
      Seq(file)
    }
  )
}