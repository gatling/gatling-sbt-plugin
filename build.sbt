import scala.util.Properties.envOrNone

import io.gatling.build.license._

disablePlugins(SonatypeReleasePlugin, MavenPublishPlugin, Sonatype)

name := "gatling-sbt"

resolvers := envOrNone("CI").map(_ => Seq(Opts.resolver.sonatypeSnapshots)).getOrElse(Seq.empty)

license := ApacheV2

scalaVersion := "2.10.6"

sbtPlugin := true

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"

scalacOptions -= "-Ybackend:GenBCode"

crossSbtVersions := Seq("1.0.0", "0.13.16")
