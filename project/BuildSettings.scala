import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import net.virtualvoid.sbt.graph.Plugin.graphSettings
import sbt.ScriptedPlugin._

import Resolvers._

object BuildSettings {

  lazy val basicSettings = Seq(
    homepage             := Some(new URL("http://gatling.io")),
    organization         := "io.gatling",
    organizationHomepage := Some(new URL("http://gatling.io")),
    startYear            := Some(2011),
    licenses             := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    scalaVersion         := "2.10.4",
    crossPaths           := false,
    resolvers            += Resolver.sonatypeRepo("snapshots"),
    scalacOptions        := Seq(
      "-encoding",
      "UTF-8",
      "-target:jvm-1.6",
      "-deprecation",
      "-feature",
      "-unchecked"
    )
  ) ++ Publish.sonatypeSettings // Switch to Publish.bintraySettings when releasing

  lazy val gatlingSbtModuleSettings =
    basicSettings ++ formattingSettings ++ graphSettings

  lazy val noCodeToPublish = Seq(
    publishArtifact in Compile := false
  )

  lazy val pluginSettings = scriptedSettings ++ Seq(
    sbtPlugin           := true,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value),
    scriptedBufferLog   := true
  )

  /*************************/
  /** Formatting settings **/
  /*************************/

  lazy val formattingSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences := formattingPreferences
  )

  import scalariform.formatter.preferences._

  def formattingPreferences =
    FormattingPreferences()
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(IndentLocalDefs, true)
}
