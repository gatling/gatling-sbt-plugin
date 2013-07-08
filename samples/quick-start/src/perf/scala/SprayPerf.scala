// package perf

// import gatling.sbt.PerfTest
// import io.gatling.http.Predef._
// import io.gatling.core.Predef._

// import scala.concurrent.duration._

// import akkaio.http.SimpleSprayApp

// class SprayPerf extends PerfTest {
//   lazy val app = SimpleSprayApp()

//   lazy val pre:Unit = app.start
//   lazy val post:Unit = app.stop


//   val httpConf = http.baseURL("http://localhost:1111/ping")
//                       .acceptHeader("*/*")
//                       .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.3")

//   val scn =
//       scenario("spray")
//         .exec(
//           http("request_1")
//             .get("/")
//             .check(status.is(200))
//         )

//   setUp(scn.inject(
//             atOnce(5 users),
//             ramp(2 users) over (5 seconds),
//             constantRate(4 usersPerSec) during (10 seconds)
//         ))
//         .protocols(httpConf)
// }

object T12345 {}