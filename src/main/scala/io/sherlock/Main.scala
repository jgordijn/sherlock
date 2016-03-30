package io.sherlock

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import io.sherlock.core.Services
import io.sherlock.http.Routes

object Main extends App {
  implicit val system = ActorSystem("sherlock")
  implicit val materializer = ActorMaterializer()

  val services = system.actorOf(Services.props, "services")

  val routes = new Routes(services).route
  val hostname = "localhost"
  val port = system.settings.config.getInt("port")

  val bindingFuture = Http().bindAndHandle(routes, hostname, port)
  import system.dispatcher
  bindingFuture.onSuccess {
    case binding => println(s"Bound: ${binding.localAddress.getPort}")
  }

}

