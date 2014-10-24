import sbt._
import sbt.Keys._

import scala.util.Properties.envOrNone

import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import net.virtualvoid.sbt.graph.Plugin.graphSettings
import sbt.ScriptedPlugin._

object BuildSettings {

  lazy val basicSettings = Seq(
    homepage             := Some(new URL("http://gatling.io")),
    organization         := "io.gatling",
    organizationHomepage := Some(new URL("http://gatling.io")),
    startYear            := Some(2011),
    licenses             := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    crossPaths           := false,
    resolvers            := envOrNone("CI").map(_ => Seq(Opts.resolver.sonatypeSnapshots)).getOrElse(Seq.empty),
    updateOptions         := updateOptions.value.withLatestSnapshots(false),
    scalacOptions        := Seq(
      "-encoding",
      "UTF-8",
      "-target:jvm-1.7",
      "-deprecation",
      "-feature",
      "-unchecked"
    )
  )

  lazy val gatlingSbtModuleSettings =
    basicSettings ++ formattingSettings ++ graphSettings

  lazy val noPublishing = Seq(
    publish := (),
    publishLocal := ()
  )

  lazy val pluginSettings = scriptedSettings ++ Seq(
    sbtPlugin           := true,
    scriptedLaunchOpts ++= Seq("-Xmx512m", "-XX:MaxPermSize=256m", "-Dplugin.version=" + version.value),
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
