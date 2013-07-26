package perf

import scala.concurrent.duration._

import com.typesafe.config.{ Config, ConfigFactory }

import gatling.sbt.PerfTest
import io.gatling.http.Predef._
import io.gatling.core.config.Protocol
import io.gatling.core.Predef._


class GooglePerf extends PerfTest {
  private[this] val classLoader = getClass.getClassLoader

  lazy val config = ConfigFactory.parseResources(classLoader, "misc.conf")

  val pre = ()
  val post = ()


  implicit class SH(s:String) {
    def trimToOption = s.trim match {
      case "" => None
      case string => Some(string)
    }    
  }

  val httpConf = {
    val t = http.baseURL("http://www.google.com")
              .acceptHeader("*/*")
              .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.3")
      
    val proxied:Option[Protocol] = for {
      host <- config.getString("proxy.host").trimToOption
      port <- config.getString("proxy.port.http").trimToOption.map{_.toInt}
    } yield {
      val p = t.proxy(host, port)
      
      val withCred:Option[Protocol] = for {
        user <- config.getString("proxy.credentials.user").trimToOption
        pwd <- config.getString("proxy.credentials.pwd").trimToOption
      } yield p.credentials(user, pwd)

      withCred.getOrElse(p)
    }
    
    val p:Protocol = t

    proxied.getOrElse(p)
  }

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