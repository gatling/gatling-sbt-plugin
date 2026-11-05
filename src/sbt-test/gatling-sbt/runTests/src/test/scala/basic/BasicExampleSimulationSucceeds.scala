package basic

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicExampleSimulationSucceeds extends Simulation {
  val httpProtocol = http
    .baseUrl("https://api-ecomm.gatling.io")
    .acceptHeader("application/json")

  val scn = scenario("Scenario name")
    .group("Login") {
      exec(http("Session").get("/session"))
    }

  setUp(scn.inject(atOnceUsers(1)))
    .assertions(global.failedRequests.count.lt(1))
    .protocols(httpProtocol)
}
