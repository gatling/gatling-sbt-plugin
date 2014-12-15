import scala.util.Properties.{ envOrNone, propOrEmpty }
import sbtrelease.ReleasePlugin.ReleaseKeys._

homepage             := Some(new URL("http://gatling.io"))
organization         := "io.gatling"
organizationHomepage := Some(new URL("http://gatling.io"))
startYear            := Some(2011)
licenses             := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
resolvers            := envOrNone("CI").map(_ => Seq(Opts.resolver.sonatypeSnapshots)).getOrElse(Seq.empty)

sbtPlugin := true

scalacOptions := Seq(
  "-encoding",
  "UTF-8",
  "-target:jvm-1.7",
  "-deprecation",
  "-feature",
  "-unchecked"
)

libraryDependencies += "org.specs2" %% "specs2" % "2.3.12" % "test"

releaseSettings

releaseVersion := { _ => propOrEmpty("releaseVersion")}
nextVersion    := { _ => propOrEmpty("developmentVersion")}
