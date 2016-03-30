package io.sherlock.core

import akka.actor.{ Actor, ActorRef, Props }
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{ DistributedData, ORSet, ORSetKey, Replicator }

object ServiceInstance {
  case class Accuracy(percentage: Long)
  val props = Props(new ServiceInstance)
}

class ServiceInstance extends Actor {
  import ServiceInstance._

  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)
  val DataKey = ORSetKey[Long](self.path.name)

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
      val all = data.elements.toList.sorted
      val first = all.head
      val diffs = all.foldLeft((Vector.empty[Long], first)) {
        case ((deltas, last), next) ⇒
          (deltas :+ (next - last), next)
      }._1.sorted.tail
      val median = diffs(diffs.size / 2)
      val now = System.currentTimeMillis()
      val timeSinceLastHeartBeat = now - all.last
      //      println(s"\t\t\t\tMedian: $median ($diffs), elapsed: $timeSinceLastHeartBeat")
      val accuracy = if (timeSinceLastHeartBeat <= median) 100 else 100 - 5 * (timeSinceLastHeartBeat / median)
      originalSender ! Accuracy(Math.max(accuracy, 0L))
  }
}
