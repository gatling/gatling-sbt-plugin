package basic

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicExampleSimulationSucceeds extends Simulation {

	val httpProtocol = http
		.baseURL("http://excilysbank.gatling.cloudbees.net")
		.acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.7")
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
		.disableFollowRedirect

	val headers_1 = Map(
		"Keep-Alive" -> "115")

	val headers_3 = Map(
		"Keep-Alive" -> "115",
		"Content-Type" -> "application/x-www-form-urlencoded")

	val headers_6 = Map(
		"Accept" -> "application/json, text/javascript, */*; q=0.01",
		"Keep-Alive" -> "115",
		"X-Requested-With" -> "XMLHttpRequest")

	val scn = scenario("Scenario name")
		.group("Login") {
			exec(
				http("request_1")
					.get("/")
					.headers(headers_1)
					.check(status.is(302)))
		}

	setUp(scn.inject(atOnceUsers(1)))
		.protocols(httpProtocol)
		.assertions(global.successfulRequests.percent.greaterThan(0))
}
