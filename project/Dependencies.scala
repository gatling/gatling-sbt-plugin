import sbt._

object Dependencies {

  private val testInterface     = "org.scala-sbt"         % "test-interface"            % "1.0"

  private val gatlingHighcharts = "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.0.0-RC5"      % "provided"

  private val specs2            = "org.specs2"           %% "specs2"                    % "2.3.12"         % "test"

  val testFrameworkDeps = Seq(gatlingHighcharts, testInterface)

  val pluginDeps = Seq(gatlingHighcharts, specs2)
}
