import sbt._
import sbt.Keys._

import bintray.Plugin.bintrayPublishSettings
import bintray.Keys._

object Publish {

  lazy val sonatypeSettings = Seq(
    publishMavenStyle := true,
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
    publishTo := Some(Resolver.sonatypeRepo("snapshots"))
  )

  lazy val bintraySettings = Seq(
    publishMavenStyle := false,
    bintrayOrganization in bintray := None,
    repository in bintray := "sbt-plugins"
  ) ++ bintrayPublishSettings
}
