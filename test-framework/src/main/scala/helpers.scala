package gatling.sbt

import scala.concurrent.Future

import io.gatling.core.Predef._

trait PerfTest extends Simulation {
  def pre:Future[Boolean]

  def post:Future[Boolean]
}