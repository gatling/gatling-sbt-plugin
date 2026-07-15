package basic

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import lib.SimulationConfig

class MultiModuleSimulation extends Simulation {
  val httpProtocol = http
    .baseUrl(SimulationConfig.BaseUrl)
    .disableFollowRedirect

  val scn = scenario("Scenario name")
    .group("Login") {
      exec(http("request_1").get("/").check(status.is(303)))
    }

  setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}
