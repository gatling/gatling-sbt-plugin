enablePlugins(GatlingPlugin)

scalaVersion := "2.11.6"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.0-SNAPSHOT" % "it,test"
libraryDependencies += "io.gatling" % "gatling-test-framework" % "2.2.0-SNAPSHOT" % "it,test"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.5"
