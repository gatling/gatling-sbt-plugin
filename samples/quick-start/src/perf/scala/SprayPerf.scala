package perf

import scala.concurrent.duration._
import scala.concurrent.{Future, Await}


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
    implicit val timout:Timeout = Timeout(1000)
    (act ? "isConnected").mapTo[Boolean].flatMap { 
      case false => checkConnected(act)
      case true => Future.successful(true)
    }
  }

  lazy val pre = checkConnected(app.start)

  lazy val post = {
    app.stop
    Future.successful(true)
  }


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