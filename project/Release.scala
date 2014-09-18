import com.typesafe.sbt.SbtPgp.PgpKeys._
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._

object Release {

  lazy val releaseToCentralSettings = releaseSettings ++ Seq(
    crossBuild := false,
    publishArtifactsAction := publishSigned.value
  )
  
  lazy val releaseToBintraySettings = releaseSettings
}
