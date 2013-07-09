/**
 * Thanks to https://github.com/spray/spray/blob/v1.2-M8/examples/spray-routing/on-spray-can/src/main/scala/spray/examples/DemoService.scala
 * and Heiko's blog entries
 */
package akkaio.http

import akka.actor.{ ActorSystem, Actor, ActorRef, ActorLogging, Props, Terminated  }
import akka.pattern.ask

import java.net.InetSocketAddress

import scala.concurrent.duration._

import akka.io.{ IO, Tcp }

import spray.can.Http
import spray.can.server.Stats
import spray.util._
import spray.http._
import spray.http.HttpCharsets._
import spray.http.HttpMethods._
import spray.http.MediaTypes._

import spray.httpx.unmarshalling.pimpHttpEntity
import spray.httpx.marshalling._

//import spray.json.DefaultJsonProtocol

import spray.routing._
//import ExceptionHandler._

import spray.routing.directives.CachingDirectives
import CachingDirectives._

case class SimpleSprayApp() {

  lazy val system = ActorSystem("http-service-system")

  def start = system.actorOf(HttpServiceActor("localhost", 1111), "http-service")

  def stop  = system.shutdown()

}

object HttpServiceActor {
  def apply(host:String, port:Int):Props = Props(new HttpServiceActor(host, port))
}

class HttpServiceActor(host:String, port:Int) extends Actor with SampleHttpService with  ActorLogging {

  def actorRefFactory = context

  def receive = runRoute(sampleRoute) orElse {
    case Http.Connected(remote, _) =>
      log.debug("Remote address {} connected", remote)
      sender ! Http.Register(context.actorOf(EchoConnectionHandler(remote, sender)))
  }

  import context.system
  //create an I/O manager for TCP in its constructor and make "self" (this actor instance) to listen for TCP connections
  IO(Http) ! Http.Bind(self, host, port)
}


// this trait defines our service behavior independently from the service actor
trait SampleHttpService extends HttpService {

  // we use the enclosing ActorContext's or ActorSystem's dispatcher for our Futures and Scheduler
  implicit def executionContext = actorRefFactory.dispatcher

  val sampleRoute = {
    get {
      path("") {
        respondWithMediaType(`text/html`) {
          complete(index)
        }
      } ~
      path("ping") {
        complete("PONG!")
      }  ~
      path("stats") {
        complete {
          actorRefFactory.actorFor("/user/IO-HTTP/listener-0")
            .ask(Http.GetStats)(1.second)
            .mapTo[Stats]
        }
      }
    }
  }


  lazy val index =
    <html>
      <body>
        <h1>Spray Perf!</h1>
        <ul>
          <li><a href="/ping">/ping</a></li>
          <li><a href="/stats">/stats</a></li>
        </ul>
      </body>
    </html>


  implicit val statsMarshaller: Marshaller[Stats] =
    Marshaller.delegate[Stats, String](ContentTypes.`text/plain`) { stats =>
      "Uptime                : " + stats.uptime.formatHMS + '\n' +
      "Total requests        : " + stats.totalRequests + '\n' +
      "Open requests         : " + stats.openRequests + '\n' +
      "Max open requests     : " + stats.maxOpenRequests + '\n' +
      "Total connections     : " + stats.totalConnections + '\n' +
      "Open connections      : " + stats.openConnections + '\n' +
      "Max open connections  : " + stats.maxOpenConnections + '\n' +
      "Requests timed out    : " + stats.requestTimeouts + '\n'
    }

}

object EchoConnectionHandler {
  def apply(remote:InetSocketAddress, connection:ActorRef):Props = Props(new EchoConnectionHandler(remote, connection))
}

class EchoConnectionHandler(remote: InetSocketAddress, connection: ActorRef) extends Actor with  ActorLogging {
  // We need to know when the connection dies without sending a `Http.ConnectionClosed`
  context.watch(connection)

  def receive = {
    case HttpRequest(GET, uri, _, _, _) =>
      sender ! HttpResponse(entity = uri.path.toString)
    case _:Tcp.ConnectionClosed =>
      log.debug("Stopping, because connection for remote address {} closed", remote)
      context.stop(self)
    case Terminated(`connection`) =>
      log.debug("Stopping, because connection for remote address {} died", remote)
      context.stop(self)

  }

}