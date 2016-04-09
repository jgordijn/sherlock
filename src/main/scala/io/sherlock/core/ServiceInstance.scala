package io.sherlock.core

import akka.actor.{ Actor, ActorRef, Props }
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{ DistributedData, ORSet, ORSetKey, Replicator }
import io.sherlock.detector.MyPhiAccrualFailureDetector

object ServiceInstance {
  case class Accuracy(percentage: Double)
  val props = Props(new ServiceInstance)

}

class ServiceInstance extends Actor {
  import ServiceInstance._

  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)
  val DataKey = ORSetKey[Long](self.path.name)
  implicit val clock = MyPhiAccrualFailureDetector.defaultClock

  var upProbability = 100
  val maxSize = 1000

  def addHeartBeat(heartBeats: ORSet[Long]): ORSet[Long] = {
    val newHeartBeat = System.currentTimeMillis()
    val truncated = if (heartBeats.size > maxSize) heartBeats - heartBeats.elements.toList.sorted.head else heartBeats
    truncated + newHeartBeat
  }

  def receive = {
    case _: HeartBeat ⇒
      replicator ! Update(DataKey, ORSet.empty[Long], WriteLocal)(addHeartBeat)
    case Service.GetAccuracy ⇒
      replicator ! Replicator.Get(DataKey, ReadLocal, Some(sender()))
    case g @ GetSuccess(DataKey, Some(originalSender: ActorRef)) ⇒
      val data = g.get(DataKey)
      val timestamps = data.elements.toList
      val accuracy = MyPhiAccrualFailureDetector(timestamps.sorted.toIndexedSeq).phi
      originalSender ! Accuracy(accuracy)
  }
}
