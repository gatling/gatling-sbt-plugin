addSbtPlugin("io.gatling"   % "gatling-build-plugin" % "7.3.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo"        % "0.14.0")

libraryDependencies ++= Seq(
  "org.scala-sbt"      %% "scripted-plugin"                % sbtVersion.value,
  "com.github.xuwei-k" %% "scala-version-from-sbt-version" % "0.1.0"
)
