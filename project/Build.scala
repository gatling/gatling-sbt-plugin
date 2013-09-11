import sbt._
import Keys._

object GatlingSbtBuild extends Build {

  lazy val gatlingProject = Project(
    id = "root", 
    base = file("."), 
    aggregate = Seq(testFramework, gatlingPlugin, quickStart),
    settings = Defaults.defaultSettings ++ buildSettings)

  lazy val testFramework = ProjectRef(file("./test-framework"), "gatling-sbt")
  lazy val gatlingPlugin = ProjectRef(file("./plugin"), "gatling-sbt")
  lazy val quickStart = ProjectRef(file("./samples/quick-start"), "gatling-sbt-quickstart")
  
  lazy val buildSettings = Seq(
    organization := "gatling",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.10.2"
  )
}