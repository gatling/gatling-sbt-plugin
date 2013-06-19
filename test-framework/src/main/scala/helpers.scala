package gatling.sbt

import io.gatling.core.Predef._

trait PerfTest extends Simulation {
  def pre:Unit

  def post:Unit
}