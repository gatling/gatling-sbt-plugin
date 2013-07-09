package perf

import gatling.sbt.PerfTest
import io.gatling.http.Predef._
import io.gatling.core.Predef._

import scala.concurrent.duration._


class GooglePerf extends PerfTest {
  val pre = ()
  val post = ()

  val httpConf = http.baseURL("http://www.google.com")
                      .acceptHeader("*/*")
                      .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.3")

  val scn =
     scenario("GGL")
        .exec(
          http("request_1")
            .get("/")
            .check(status.is(200))
        )

  setUp(scn.inject(
            atOnce(5 users),
            ramp(2 users) over (5 seconds),
            constantRate(4 usersPerSec) during (10 seconds)
        ))
        .protocols(httpConf)
}