ThisBuild / Keys.useCoursier := false

enablePlugins(SbtPlugin)
enablePlugins(GatlingOssPlugin)

name := "gatling-sbt"
scalaVersion := "2.12.12"
sbtPlugin := true
githubPath := "gatling/gatling-sbt-plugin"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % "test"

scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq(
      "-Xmx512m",
      "-Dgatling.http.enableGA=false",
      "-Dplugin.version=" + version.value
    )
}

scriptedBufferLog := false

pluginCrossBuild / sbtVersion := "1.4.3"
