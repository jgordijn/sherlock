package io.sherlock.core

import akka.actor.{ Actor, ActorRef, Props }
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata._
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

object Service {
  def props = Props(new Service)
  case object GetAccuracy
  case class Result(accuracy: Map[String, Long])
}

class Service extends Actor {
  import Service._
  import context.dispatcher
  implicit val timeout: Timeout = 2.seconds

  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)
  val DataKey = ORSetKey[String](self.path.name)
  replicator ! Subscribe(DataKey, self)

  def getOrCreate(serviceInstanceName: String): ActorRef = context.child(serviceInstanceName).getOrElse(context.actorOf(ServiceInstance.props, serviceInstanceName))
  def getOrCreateAndSubscribe(serviceInstanceName: String): ActorRef = context.child(serviceInstanceName).getOrElse {
    replicator ! Update(DataKey, ORSet(), WriteLocal)(_ + serviceInstanceName)
    context.actorOf(ServiceInstance.props, serviceInstanceName)
  }

  def receive = {
    case hb @ HeartBeat(root, ip, port) ⇒
      val serviceInstanceName = s"$ip:$port"
      getOrCreateAndSubscribe(serviceInstanceName) ! hb
    case GetAccuracy ⇒
      val futures = Future.traverse(context.children) { child ⇒ (child ? GetAccuracy).mapTo[ServiceInstance.Accuracy].map(a ⇒ child.path.name -> a.percentage) }
      futures.map(_.toMap).map(Result).pipeTo(sender())
    case c @ Changed(DataKey) ⇒
      val data = c.get(DataKey)
      data.elements.foreach(getOrCreate)
  }
}

