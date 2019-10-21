import scala.util.Properties.envOrNone

import _root_.io.gatling.build.license._

disablePlugins(SonatypeReleasePlugin, MavenPublishPlugin, Sonatype)

name := "gatling-sbt"

resolvers := envOrNone("CI").map(_ => Seq(Opts.resolver.sonatypeSnapshots)).getOrElse(Seq.empty)

headerLicense := ApacheV2License

scalaVersion := "2.12.10"

sbtPlugin := true

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

scalacOptions -= "-Ybackend:GenBCode"

scriptedLaunchOpts :=
  Seq(
    "-Xmx512m",
    "-Dgatling.http.enableGA=false",
    "-Dplugin.version=" + version.value
  )

scriptedBufferLog := false
