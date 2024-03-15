enablePlugins(GatlingPlugin)

name := "my-test-project"
version := "1.2.3"

scalaVersion := "2.13.13"

val gatlingVersion = "3.10.4"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "it,test"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "it,test"
