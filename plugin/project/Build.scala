import sbt._
import Keys._

object GatlingSbtBuild extends Build {

  lazy val gatlingProject = Project("gatling-sbt", file("."), settings = gatlingSettings)

  lazy val gatlingSettings = Defaults.defaultSettings ++ Seq(
    sbtPlugin := true,
    organization := "gatling",
    name := "gatling-sbt-plugin",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.10.2",
    //todo: revisit, on final versions
    publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository"))),
    resolvers += "Local Maven Repository" at file(Path.userHome.absolutePath+"/.m2/repository").toURI.toURL.toString,
    resolvers += "Gatling Cloudbees" at "http://repository-gatling.forge.cloudbees.com/snapshot",
    libraryDependencies += "gatling" %% "gatling-sbt-test-framework" % "0.0.1-SNAPSHOT"
  )
}