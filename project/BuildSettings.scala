import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import net.virtualvoid.sbt.graph.Plugin.graphSettings

import Resolvers._

object BuildSettings {

	lazy val basicSettings = Seq(
		homepage              := Some(new URL("http://gatling.io")),
		organization          := "io.gatling",
		organizationHomepage  := Some(new URL("http://gatling.io")),
		startYear             := Some(2011),
		licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.html")),
		scalaVersion          := "2.10.3",
		crossPaths            := false,
		resolvers             := Seq(sonatypeSnapshots),
		scalacOptions         := Seq(
			"-encoding",
			"UTF-8",
			"-target:jvm-1.6",
			"-deprecation",
			"-feature",
			"-unchecked"
		)
	)

	lazy val gatlingSbtModuleSettings = 
		basicSettings ++ formattingSettings ++ graphSettings

	lazy val noCodeToPublish = Seq(
		publishArtifact in Compile := false
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
