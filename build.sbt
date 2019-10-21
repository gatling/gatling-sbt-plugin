import scala.util.Properties.envOrNone

import _root_.io.gatling.build.license._

enablePlugins(SbtPlugin)
disablePlugins(SonatypeReleasePlugin, MavenPublishPlugin, Sonatype)

name := "gatling-sbt"

resolvers := envOrNone("CI").map(_ => Seq(Opts.resolver.sonatypeSnapshots)).getOrElse(Seq.empty)

headerLicense := ApacheV2License

scalaVersion := "2.12.10"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

scalacOptions -= "-Ybackend:GenBCode"

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq(
    "-Xmx512m",
    "-Dgatling.http.enableGA=false",
    "-Dplugin.version=" + version.value
  )
}

scriptedBufferLog := false
