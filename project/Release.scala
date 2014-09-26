import scala.util.Properties.propOrEmpty

import com.typesafe.sbt.SbtPgp.PgpKeys._
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._

object Release {

  private lazy val commonReleaseSettings = releaseSettings ++ Seq(
    releaseVersion := { _ => propOrEmpty("releaseVersion")},
    nextVersion := { _ => propOrEmpty("developmentVersion")}
  )

  lazy val releaseToCentralSettings = commonReleaseSettings ++ Seq(
    crossBuild := false,
    publishArtifactsAction := publishSigned.value
  )
  
  lazy val releaseToBintraySettings = commonReleaseSettings
}
