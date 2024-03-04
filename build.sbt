enablePlugins(BuildInfoPlugin, SbtPlugin, GatlingOssPlugin)

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

name := "gatling-sbt"
scalaVersion := "2.13.13"
sbtPlugin := true
githubPath := "gatling/gatling-sbt-plugin"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"                         % "3.2.18" % Test,
  "io.gatling"     % "gatling-enterprise-plugin-commons" % "1.9.0-M9"
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

pluginCrossBuild / sbtVersion := "1.9.8"
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
