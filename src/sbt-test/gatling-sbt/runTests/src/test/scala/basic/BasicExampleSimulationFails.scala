package basic

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicExampleSimulationFails extends Simulation {

  val httpProtocol = http
    .baseUrl("http://computer-database.gatling.io")
    .disableFollowRedirect

  val scn = scenario("Scenario name")
    .group("Login") {
      exec(http("request_1").get("/").check(status.is(303)))
        .feed(csv("foo.csv"))
    }

  setUp(scn.inject(atOnceUsers(1)))
    .protocols(httpProtocol)
    .assertions(global.successfulRequests.percent.is(100))
}
