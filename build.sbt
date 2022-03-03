enablePlugins(BuildInfoPlugin, SbtPlugin, GatlingOssPlugin)

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

name := "gatling-sbt"
scalaVersion := "2.12.15"
sbtPlugin := true
githubPath := "gatling/gatling-sbt-plugin"

libraryDependencies ++= Seq(
  "org.scalatest"     %% "scalatest"                         % "3.2.11" % Test,
  "org.zeroturnaround" % "zt-zip"                            % "1.14",
  "io.gatling"         % "gatling-enterprise-plugin-commons" % "1.0.2"
)

scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq(
      "-Xmx512m",
      "-Dgatling.http.enableGA=false",
      "-Dplugin.version=" + version.value
    )
}

scriptedBufferLog := false

pluginCrossBuild / sbtVersion := "1.5.5"
gatlingDevelopers := Seq(
  GatlingDeveloper(
    "slandelle@gatling.io",
    "Stéphane Landelle",
    true
  ),
  GatlingDeveloper(
    "sbrevet@gatling.io",
    "Sébastien Brevet",
    true
  )
)

buildInfoKeys := Seq[BuildInfoKey](name, version)
buildInfoPackage := "io.gatling.sbt"
