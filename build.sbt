// Scala 2.12 builds the plugin for sbt 1.x, Scala 3 builds it for sbt 2.x.
// sbt 2.0.x is built with Scala 3.8.4, and the plugin must use that exact version so it can read sbt's TASTy.
val scala212 = "2.12.21"
val scala3 = "3.8.4"

lazy val gatlingSbt = rootProject
  .enablePlugins(BuildInfoPlugin, SbtPlugin, GatlingOssPlugin)
  .settings(
    name := "gatling-sbt",
    scalaVersion := scala212,
    crossScalaVersions := Seq(scala212, scala3),
    sbtPlugin := true,
    githubPath := "gatling/gatling-sbt-plugin",
    sbtPluginPublishLegacyMavenStyle := false,

    // GatlingPublishPlugin sets `crossPaths := false`, which disables sbt's automatic `scala-<version>` source
    // directories. We add them back explicitly so sbt-1-only (Scala 2.12) and sbt-2-only (Scala 3) sources can
    // coexist: `src/main/scala-2.12` for sbt 1.x and `src/main/scala-3` for sbt 2.x.
    Compile / unmanagedSourceDirectories += {
      val base = (Compile / sourceDirectory).value
      scalaBinaryVersion.value match {
        case "2.12" => base / "scala-2.12"
        case _      => base / "scala-3"
      }
    },

    // sbt 1.x supports JDK 8+, so the Scala 2.12 build targets Java 11; sbt 2.x requires JDK 17+, and Scala 3
    // on recent JDKs no longer accepts `-release 11`, so the Scala 3 build targets Java 17.
    gatlingCompilerRelease := {
      scalaBinaryVersion.value match {
        case "2.12" => 11
        case _      => 17
      }
    },

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest"                         % "3.2.20" % Test,
      "io.gatling"     % "gatling-enterprise-plugin-commons" % "1.25.1",
      "io.gatling"     % "gatling-shared-cli"                % "0.0.7"
    ),

    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq(
          "-Xmx512m",
          "-Dgatling.data.enableAnalytics=false",
          "-Dplugin.version=" + version.value
        )
    },

    scriptedBufferLog := false,

    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.12.13"
        case _      => "2.0.2"
      }
    },
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
    ),

    buildInfoKeys := Seq[BuildInfoKey](name, version),
    buildInfoPackage := "io.gatling.sbt"
  )
