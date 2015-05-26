import scala.util.Properties.envOrNone

import io.gatling.build.license._

enablePlugins(BintrayReleasePlugin)

resolvers := envOrNone("CI").map(_ => Seq(Opts.resolver.sonatypeSnapshots)).getOrElse(Seq.empty)

license := ApacheV2

scalaVersion := "2.10.5"

sbtPlugin := true

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.5" % "test"
