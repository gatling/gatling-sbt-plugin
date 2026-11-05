package basic

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import lib.SimulationConfig

class MultiModuleSimulation extends Simulation {
  val httpProtocol = http
    .baseUrl(SimulationConfig.BaseUrl)
    .acceptHeader("application/json")

  val scn = scenario("Scenario 1")
    .exec(http("Session").get("/session"))

  setUp(scn.inject(atOnceUsers(1)))
    .assertions(global.failedRequests.count.lt(1))
    .protocols(httpProtocol)
}
