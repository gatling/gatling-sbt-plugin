enablePlugins(GatlingPlugin)

name := "my-test-project"
version := "1.2.3"

scalaVersion := "2.13.12"

val gatlingVersion = "3.9.5"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "it,test"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "it,test"
