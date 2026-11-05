package basic

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicItExampleSimulationFails extends Simulation {
  val httpProtocol = http
    .baseUrl("https://api-ecomm.gatling.io")
    .acceptHeader("application/json")

  val scn = scenario("Scenario name")
    .group("Login") {
      // deliberately wrong expected status, so that the simulation fails
      exec(http("Session").get("/session").check(status.is(404)))
        .feed(csv("foo.csv"))
    }

  setUp(scn.inject(atOnceUsers(1)))
    .protocols(httpProtocol)
    .assertions(global.successfulRequests.percent.is(100))
}
