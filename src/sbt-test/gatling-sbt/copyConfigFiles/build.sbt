enablePlugins(GatlingPlugin)

scalaVersion := "2.13.10"

val gatlingVersion = "3.3.0"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "it,test"
libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion % "it,test"
