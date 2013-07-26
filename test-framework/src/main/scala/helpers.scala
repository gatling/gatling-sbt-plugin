package gatling.sbt

import scala.concurrent.Future
import scala.concurrent.duration._

import io.gatling.core.Predef._

trait PerfTest extends Simulation {
  def maxInitializationTime:Duration = 3 seconds

  def pre:Future[Boolean]

  def post:Future[Boolean]
}