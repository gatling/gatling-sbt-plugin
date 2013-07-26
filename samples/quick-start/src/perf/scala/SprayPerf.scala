package perf

import scala.concurrent.duration._
import scala.concurrent.{Future, Await}
//import scala.concurrent.ExecutionContext.Implicits.global


import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import gatling.sbt.PerfTest
import io.gatling.http.Predef._
import io.gatling.core.Predef._

import akkaio.http.SimpleSprayApp

class SprayPerf extends PerfTest {

  implicit val ex =  io.gatling.core.action.system.dispatcher

  lazy val app = SimpleSprayApp()
  
  def checkConnected(act:ActorRef):Future[Boolean] = {
    implicit val timout:Timeout = Timeout(10)
    (act ? "isConnected").flatMap {
      case false => checkConnected(act)
      case true => Future.successful(true)
    }
  }

  lazy val pre:Unit = {
    val act= app.start
    val checking = checkConnected(act)
    val d:Duration = 1 milliseconds
    val started = Await.result(checking, d)
    if (!started) {
      throw new IllegalStateException(s"The Spray App didn't started after $d")
    }
  }
  lazy val post:Unit = app.stop



  val httpConf = http.baseURL("http://localhost:1111/ping")
                      .acceptHeader("*/*")
                      .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.3")
  val scn =
      scenario("spray")
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