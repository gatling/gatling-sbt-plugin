import sbt._
import sbt.Keys._

import io.gatling.sbt.GatlingPlugin._

object TestBuild extends Build {

  val libs = Seq(
    "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.0.0-SNAPSHOT" % "it,test",
    "io.gatling" % "gatling-bundle" % "2.0.0-SNAPSHOT" % "test" artifacts (Artifact("gatling-bundle", "zip", "zip", "bundle")),
    "io.gatling" % "test-framework" % "1.0-SNAPSHOT" % "it,test"
  )

  val root = Project("sbt-test", file("."))
    .settings(gatlingAllSettings: _*)
    .configs(IntegrationTest, Gatling, GatlingIt)
    .settings(organization := "io.gatling.sbt.test")
    .settings(libraryDependencies ++= libs)
}
