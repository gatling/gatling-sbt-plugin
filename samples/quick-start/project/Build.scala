import sbt._
import Keys._
import gatling.sbt.GatlingPlugin


object MinimalBuild extends Build {
  val appName = "gatling-sbt-quickstart"
  val buildVersion =  "0.0.1-SNAPSHOT"


  val localIvyRepo =  Resolver.file("Local ivy2 Repository", file(Path.userHome.absolutePath+"/.ivy2/local"))(Resolver.ivyStylePatterns)

  val gatSbtTestVersion = "0.0.1-SNAPSHOT"

  val libDependencies = Seq(
    "gatling" %% "gatling-sbt-test-framework" % gatSbtTestVersion % "perf",

    "com.typesafe.akka" %% "akka-actor" % "2.2.0",
    "com.typesafe.akka" %% "akka-slf4j" % "2.2.0",
    "ch.qos.logback" % "logback-classic" % "1.0.13",
    "io.spray" % "spray-routing" % "1.2-M8",
    "io.spray" % "spray-can" % "1.2-M8"
  )


  lazy val allSettings =
    Project.defaultSettings ++
    GatlingPlugin.gatlingSettings ++
    Seq(
      scalaVersion := "2.10.1",
      version := buildVersion,
      organization := "gatling",
      resolvers += localIvyRepo,
      resolvers += "spray repo" at "http://repo.spray.io",
      resolvers += "Local Maven Repository" at file(Path.userHome.absolutePath+"/.m2/repository").toURI.toURL.toString,
      javacOptions += "-Xlint:unchecked",
      libraryDependencies ++= libDependencies
    )

  lazy val root = Project(id = appName, base = file(".")).configs(GatlingPlugin.PerfTest).settings(allSettings: _*)

}