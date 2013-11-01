import sbt._
import sbt.Keys._

import io.gatling.sbt.GatlingPlugin.{ Gatling, gatlingSettings }

object TestBuild extends Build {

val libs = Seq(
	"io.gatling.highcharts" % "gatling-charts-highcharts" % "2.0.0-SNAPSHOT" % "test",
	"io.gatling" % "test-framework" % "1.0-SNAPSHOT" % "test"
	)

	val root = Project("sbt-test", file("."))
				.settings(gatlingSettings: _*)
				.configs(Gatling)
				.settings(organization := "io.gatling.sbt.test")
				.settings(libraryDependencies ++= libs)
}