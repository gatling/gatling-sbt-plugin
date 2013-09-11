//allows to resolve eclipse dependency
resolvers += "Gatling Cloudbees" at "http://repository-gatling.forge.cloudbees.com/snapshot"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.3.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
