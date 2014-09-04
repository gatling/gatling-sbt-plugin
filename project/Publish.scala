import sbt._
import sbt.Keys._

import bintray.Plugin.bintrayPublishSettings
import bintray.Keys._

object Publish {

  lazy val sonatypeSettings = Seq(
    publishMavenStyle    := true,
    pomExtra             := scm ++ developersXml(developers),
    pomIncludeRepository := { _ => false },
    credentials          += Credentials(Path.userHome / ".sbt" / ".credentials"),
    publishTo            := Some(if(isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging)
  )

  lazy val bintraySettings = bintrayPublishSettings ++ Seq(
    publishMavenStyle := false,
    bintrayOrganization in bintray := None,
    repository in bintray := "sbt-plugins"
  )

  /************************/
  /** POM extra metadata **/
  /************************/

  private val scm = {
    <scm>
      <connection>scm:git:git@github.com:gatling/gatling-sbt.git</connection>
      <developerConnection>scm:git:git@github.com:gatling/gatling-sbt.git</developerConnection>
      <url>https://github.com/gatling/gatling-sbt</url>
      <tag>HEAD</tag>
    </scm>
  }

  private case class GatlingDeveloper(emailAddress: String, name: String, isEbiz: Boolean)

  private val developers = Seq(GatlingDeveloper("pdalpra@excilys.com", "Pierre Dal-pra", true))

  private def developersXml(devs: Seq[GatlingDeveloper]) = {
    <developers>
      {
      for(dev <- devs)
      yield {
        <developer>
          <id>{dev.emailAddress}</id>
          <name>{dev.name}</name>
          { if (dev.isEbiz) <organization>eBusiness Information, Excilys Group</organization> }
        </developer>
      }
      }
    </developers>
  }
}
