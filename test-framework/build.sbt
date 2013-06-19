scalaVersion := "2.10.1"

organization := "gatling"

name := "gatling-sbt-test-framework"

version := "0.0.1-SNAPSHOT"

resolvers += "Gatling Releases Repo" at "http://repository.excilys.com/content/repositories/releases"

resolvers += "Gatling Snaps Repo" at "http://repository.excilys.com/content/repositories/snapshots"

resolvers += "Gatling Third-Party Repo" at "http://repository.excilys.com/content/repositories/thirdparty"

libraryDependencies += "org.scala-tools.testing" % "test-interface" % "0.5"

libraryDependencies += "io.gatling" % "gatling-app" % "2.0.0-M2" 

libraryDependencies += "io.gatling" % "gatling-core" % "2.0.0-M2" 

libraryDependencies += "io.gatling" % "gatling-http" % "2.0.0-M2" 

libraryDependencies += "io.gatling" % "gatling-recorder" % "2.0.0-M2" 

libraryDependencies += "io.gatling" % "gatling-charts" % "2.0.0-M2" 

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.0.0-M2"