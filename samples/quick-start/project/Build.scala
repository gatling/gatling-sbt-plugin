import sbt._
import Keys._
import gatling.sbt.GatlingPlugin


object MinimalBuild extends Build {
  val appName = "gatling-sbt-quickstart"
  val buildVersion =  "0.0.1-SNAPSHOT"


  val localIvyRepo =  Resolver.file("Local ivy2 Repository", file(Path.userHome.absolutePath+"/.ivy2/local"))(Resolver.ivyStylePatterns)

  val gatSbtTestVersion = "0.0.1-SNAPSHOT"

  val libDependencies = Seq(
    "gatling" %% "gatling-sbt-test-framework" % gatSbtTestVersion % "perf"
  )


  lazy val allSettings = 
    Project.defaultSettings ++ 
    GatlingPlugin.gatlingSettings ++ 
    Seq(
      scalaVersion := "2.10.1",
      version := buildVersion,
      organization := "gatling",
      resolvers += localIvyRepo,
      javacOptions += "-Xlint:unchecked",
      libraryDependencies ++= libDependencies
    )

  lazy val root = Project(id = appName, base = file(".")).configs(GatlingPlugin.PerfTest).settings(allSettings: _*)
  
}