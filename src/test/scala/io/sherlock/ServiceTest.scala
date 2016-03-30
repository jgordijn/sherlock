package io.sherlock

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import HeartBeat
import io.sherlock.core.{HeartBeat, Service}
import org.scalatest.{WordSpecLike, FunSuite}

import scala.util.Random

class ServiceTest extends TestKit(ActorSystem()) with WordSpecLike with ImplicitSender {

  val service = system.actorOf(Service.props, "foo")

  "Service" should {
    "handle heartbeats" in {
      (1 to 10).foreach { _ =>
        val r = Random.nextInt(15)
        Thread.sleep(100 + r)
        service ! HeartBeat("/foo", "192.168.1.1", 80)
        Thread.sleep(10)
        service ! HeartBeat("/foo", "192.168.1.2", 80)
      }
      val start = System.currentTimeMillis()
      (1 to 10).foreach { i =>
        val r = Random.nextInt(15)
        Thread.sleep(50 + r)
        service ! HeartBeat("/foo", "192.168.1.2", 80)

        service ! Service.GetAccuracy
        val result = expectMsgType[Service.Result]
        println(s"${System.currentTimeMillis() - start}: $result")
      }


      val service2 = system.actorOf(Service.props, "foo2")
      service2 ! Service.GetAccuracy
      val result = expectMsgType[Service.Result]
      println(s"${System.currentTimeMillis() - start}: $result")
      Thread.sleep(200)
      service2 ! Service.GetAccuracy
      val result2 = expectMsgType[Service.Result]
      println(s"${System.currentTimeMillis() - start}: $result2")
      Thread.sleep(5000)
      service2 ! Service.GetAccuracy
      val result3 = expectMsgType[Service.Result]
      println(s"${System.currentTimeMillis() - start}: $result3")



    }
  }


}
