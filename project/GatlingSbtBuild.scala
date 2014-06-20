import sbt._
import sbt.Keys._

import BuildSettings._
import Dependencies._

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
    .settings(noCodeToPublish: _*)

  /*************/
  /** Modules **/
  /*************/

  def gatlingSbtModule(id: String) =
    Project(id, file(id)).settings(gatlingSbtModuleSettings: _*)

  lazy val testFramework = gatlingSbtModule("test-framework")
    .settings(libraryDependencies ++= testFrameworkDeps)

  lazy val plugin = gatlingSbtModule("sbt-plugin")
    .dependsOn(testFramework)
    .settings(libraryDependencies ++= pluginDeps)
    .settings(pluginSettings: _*)
}
