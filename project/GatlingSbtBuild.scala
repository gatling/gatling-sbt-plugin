import sbt._
import sbt.Keys._

import BuildSettings._
import Dependencies._
import Publish._
import Release._

object GatlingSbtBuild extends Build {

  override lazy val settings = super.settings ++ {
    shellPrompt := { state => Project.extract(state).currentProject.id + " > "}
  }

  /******************/
  /** Root project **/
  /******************/

  lazy val root = Project("gatling-sbt", file("."))
    .aggregate(plugin, testFramework)
    .settings(basicSettings: _*)
    .settings(noPublishing: _*)

  /*************/
  /** Modules **/
  /*************/

  def gatlingSbtModule(id: String) =
    Project(id, file(id)).settings(gatlingSbtModuleSettings: _*)

  lazy val testFramework = gatlingSbtModule("test-framework")
    .settings(libraryDependencies ++= testFrameworkDeps)
    .settings(sonatypeSettings: _*)
    .settings(releaseToCentralSettings: _*)

  lazy val plugin = gatlingSbtModule("sbt-plugin")
    .dependsOn(testFramework)
    .settings(libraryDependencies ++= pluginDeps)
    .settings(pluginSettings: _*)
    .settings(bintraySettings: _*)
    .settings(releaseToBintraySettings: _*)
}
