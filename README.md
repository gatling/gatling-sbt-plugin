# gatling-sbt   [![Build Status](https://travis-ci.org/gatling/gatling-sbt.png?branch=master)](https://travis-ci.org/gatling/gatling-sbt)


This SBT plugin integrates Gatling with SBT, allowing to use Gatling as a testing framework.

## Setup 

Snapshots are available on Sonatype.

In `projects/plugins.sbt`, add: 

    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

    addSbtPlugin("io.gatling" % "sbt-plugin" % "1.0-SNAPSHOT")
    
You'll also need those two dependencies:

```scala
"io.gatling.highcharts" % "gatling-charts-highcharts" % "2.0.0-SNAPSHOT" % "test"
"io.gatling" % "test-framework" % "1.0-SNAPSHOT" % "test"
```

And then, in your `.scala` build (this currently doesn't work with `.sbt` files, this limitation will be dropped ASAP) :

```scala

import io.gatling.sbt.GatlingPlugin.{ Gatling, gatlingSettings }

lazy val project = Project(...)
                     .settings(gatlingSettings: _*)
				     .configs(Gatling)
				     .settings(libraryDependencies ++= /* gatling dependencies */)
				     .settings(resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")

```

## Usage 

As with any SBT testing framework, you'll be able to run Gatling simulations using SBT standard `test`, `testOnly`, `testQuick`, etc... tasks. Note however that they must be prefixed by `gatling:`, eg. `gatling:test`, `gatling:testOnly`, `gatling:testQuick`, etc...

## Default settings 

* By default, Gatling simulations must be in `src/test/scala`, configurable using the `scalaSource in Gatling` setting.
* By default, Gatling reports are written to `target/gatling`, configurable using the `target in Gatling` setting.
 
## Additionnal tasks

Gatling's SBT plugin also offers two others tasks:

* `gatling:startRecorder`, which start the Recorder, configured to save recorded simulations to the location specified by `scalaSource in Gatling` (by default, `src/test/scala`).
* `gatling:lastReport`, which opens the last generated report in your web browser.
