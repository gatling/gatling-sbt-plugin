import scala.util.Properties.envOrNone

enablePlugins(BintrayReleasePlugin)

resolvers := envOrNone("CI").map(_ => Seq(Opts.resolver.sonatypeSnapshots)).getOrElse(Seq.empty)

scalaVersion := "2.10.4"

sbtPlugin := true

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"