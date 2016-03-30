package io.sherlock.core

import akka.actor.{ Actor, ActorRef, Props }
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{ DistributedData, ORSet, ORSetKey }
import akka.util.Timeout

import scala.concurrent.duration._

object Services {
  val props = Props(new Services)
  case class Get(contextRoot: String)
  def rootToName(root: String): String = root.replaceAll("/", "_")
}
class Services extends Actor {
  import Services._
  implicit val timeout: Timeout = 2.seconds

  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)
  val DataKey = ORSetKey[String](self.path.name)
  replicator ! Subscribe(DataKey, self)

  def getOrCreate(serviceName: String): ActorRef = context.child(serviceName).getOrElse(context.actorOf(Service.props, serviceName))
  def getOrCreateAndSubscribe(serviceName: String): ActorRef = context.child(serviceName).getOrElse {
    replicator ! Update(DataKey, ORSet(), WriteLocal)(_ + serviceName)
    context.actorOf(Service.props, serviceName)
  }

  def receive = {
    case hb @ HeartBeat(root, ip, port) ⇒
      val serviceName = rootToName(root)
      getOrCreateAndSubscribe(serviceName) ! hb
    case c @ Changed(DataKey) ⇒
      val data = c.get(DataKey)
      data.elements.foreach(getOrCreate)
    case Get(root) ⇒
      context.child(rootToName(root)) match {
        case None    ⇒ sender() ! Service.Result(Map.empty)
        case Some(c) ⇒ c forward Service.GetAccuracy
      }
  }

}
