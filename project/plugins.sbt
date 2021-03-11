resolvers += Resolver.bintrayIvyRepo("gatling", "sbt-plugins")
resolvers += Resolver.jcenterRepo

addSbtPlugin("io.gatling" % "gatling-build-plugin"  % "2.4.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.6")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
