addSbtPlugin("io.gatling"   % "gatling-build-plugin" % "6.5.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo"        % "0.13.1")

libraryDependencies ++= Seq(
  "org.scala-sbt"      %% "scripted-plugin"                % sbtVersion.value,
  "com.github.xuwei-k" %% "scala-version-from-sbt-version" % "0.1.0"
)
