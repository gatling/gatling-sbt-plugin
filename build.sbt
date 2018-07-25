import scala.util.Properties.envOrNone

import io.gatling.build.license._

disablePlugins(SonatypeReleasePlugin, MavenPublishPlugin, Sonatype)

name := "gatling-sbt"

resolvers := envOrNone("CI").map(_ => Seq(Opts.resolver.sonatypeSnapshots)).getOrElse(Seq.empty)

license := ApacheV2

scalaVersion := "2.10.7"

sbtPlugin := true

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

scalacOptions -= "-Ybackend:GenBCode"

crossSbtVersions := Seq("1.1.6", "0.13.17")
