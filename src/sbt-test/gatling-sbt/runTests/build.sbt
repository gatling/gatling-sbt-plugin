enablePlugins(GatlingPlugin)

scalaVersion := "2.12.12"

val gatlingVersion = "3.4.1"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "it,test"
libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion % "it,test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14"
