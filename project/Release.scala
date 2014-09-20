import scala.util.Properties.{ propOrEmpty, propOrNone }

import com.typesafe.sbt.SbtPgp.PgpKeys._
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._

object Release {

  private lazy val commonReleaseSettings = releaseSettings ++ Seq(
    releaseVersion := { _ => propOrEmpty("releaseVersion")},
    nextVersion := { _ => propOrEmpty("developmentVersion")},
    pgpPassphrase := propOrNone("gpg.passphrase").map(_.toCharArray)
  )

  lazy val releaseToCentralSettings = commonReleaseSettings ++ Seq(
    crossBuild := false,
    publishArtifactsAction := publishSigned.value
  )
  
  lazy val releaseToBintraySettings = commonReleaseSettings
}
