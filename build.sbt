import scala.util.Properties.envOrNone

enablePlugins(BintrayReleasePlugin)

resolvers := envOrNone("CI").map(_ => Seq(Opts.resolver.sonatypeSnapshots)).getOrElse(Seq.empty)

scalaVersion := "2.10.4"

sbtPlugin := true

libraryDependencies += "org.specs2" %% "specs2" % "2.3.12" % "test"