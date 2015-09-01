resolvers += Resolver.url("scoverage",url("http://dl.bintray.com/sksamuel/sbt-plugins"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("gatling", url("http://dl.bintray.com/content/gatling/sbt-plugins/"))(Resolver.ivyStylePatterns)

addSbtPlugin("io.gatling" % "gatling-build-plugin" % "1.8.2")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
