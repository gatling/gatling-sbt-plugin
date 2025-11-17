enablePlugins(BuildInfoPlugin, SbtPlugin, GatlingOssPlugin)

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

name := "gatling-sbt"
scalaVersion := "2.12.20"
sbtPlugin := true
githubPath := "gatling/gatling-sbt-plugin"
sbtPluginPublishLegacyMavenStyle := false

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"                         % "3.2.19" % Test,
  "io.gatling"     % "gatling-enterprise-plugin-commons" % "1.22.0",
  "io.gatling"     % "gatling-shared-cli"                % "0.0.7"
)

scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq(
      "-Xmx512m",
      "-Dgatling.data.enableAnalytics=false",
      "-Dplugin.version=" + version.value
    )
}

scriptedBufferLog := false

pluginCrossBuild / sbtVersion := "1.11.7"
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
  ),
  GatlingDeveloper(
    "tpetillot@gatling.io",
    "Thomas Petillot",
    true
  )
)

buildInfoKeys := Seq[BuildInfoKey](name, version)
buildInfoPackage := "io.gatling.sbt"
