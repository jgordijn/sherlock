package io.sherlock.detector

import java.util.concurrent.TimeUnit._

import io.sherlock.detector.MyPhiAccrualFailureDetector.Clock
import org.scalatest.{ Matchers, WordSpec }

import scala.collection.immutable.IndexedSeq

class FailureDetectorTest extends WordSpec with Matchers {
  "ServiceInstance" should {
    "calculate probability from 1 timestamp" in {
      val result = FailureDetector.calculateUpProbability(List(System.currentTimeMillis()))
      result shouldBe 100
    }
    "d" in {
      val h = IndexedSeq(
        System.currentTimeMillis() - 5000,
        System.currentTimeMillis() - 4000,
        System.currentTimeMillis() - 3000,
        System.currentTimeMillis() - 2000,
        System.currentTimeMillis() - 1000,
        System.currentTimeMillis()
      )
      implicit val defaultClock = new Clock {
        def apply() = NANOSECONDS.toMillis(System.nanoTime)
      }

      val m = MyPhiAccrualFailureDetector(h)
      println(m.phi)
      Thread.sleep(2000)
      println(m.phi)

    }
  }
}
