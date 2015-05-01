resolvers += Resolver.url(
"gatling-sbt-plugins",
    url("http://dl.bintray.com/content/gatling/sbt-plugins/"))(Resolver.ivyStylePatterns)

addSbtPlugin("io.gatling" % "gatling-build-plugin" % "1.6.0")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
