enablePlugins(SbtPlugin)
enablePlugins(GatlingOssPlugin)

name := "gatling-sbt"
scalaVersion := "2.13.7"
sbtPlugin := true
githubPath := "gatling/gatling-sbt-plugin"

libraryDependencies ++= Seq(
  "org.scalatest"     %% "scalatest"                         % "3.2.10" % Test,
  "org.zeroturnaround" % "zt-zip"                            % "1.14",
  "io.gatling"         % "gatling-enterprise-plugin-commons" % "0.0.3"
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
