enablePlugins(GatlingPlugin)

scalaVersion := "2.13.13"

val gatlingVersion = "3.10.4"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "it,test"
libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion % "it,test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18"
