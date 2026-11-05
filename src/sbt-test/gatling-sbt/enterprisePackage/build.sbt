enablePlugins(GatlingPlugin)

name := "my-test-project"
version := "1.2.3"

scalaVersion := "2.13.18"

val gatlingVersion = "3.15.0"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "it,test"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "it,test"
