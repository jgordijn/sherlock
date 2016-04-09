package io.sherlock.detector

import scala.concurrent.duration._

object MyPhiAccrualFailureDetector {
  abstract class Clock extends (() ⇒ Long)

  val defaultClock: Clock = new Clock {
    def apply(): Long = System.currentTimeMillis()
  }
}

case class MyPhiAccrualFailureDetector(timestamps: IndexedSeq[Long])(implicit clock: MyPhiAccrualFailureDetector.Clock) {
  val intervals = timestamps.foldLeft((Vector.empty[Long], timestamps.head)) {
    case ((deltas, last), next) ⇒
      (deltas :+ (next - last), next)
  }._1.sorted.tail

  val intervalSum: Long = intervals.sum

  val squaredIntervalSum: Long = intervals.foldLeft(0L) { case (acc, interval) ⇒ acc + pow2(interval) }

  def mean: Double = intervalSum.toDouble / intervals.size

  def variance: Double = (squaredIntervalSum.toDouble / intervals.size) - (mean * mean)

  def stdDeviation: Double = math.sqrt(variance)

  private def pow2(x: Long) = x * x

  val minStdDeviation = 100.millis
  val acceptableHeartbeatPause = 200.millis
  private val minStdDeviationMillis = minStdDeviation.toMillis
  private val acceptableHeartbeatPauseMillis = acceptableHeartbeatPause.toMillis

  private def ensureValidStdDeviation(stdDeviation: Double): Double = math.max(stdDeviation, minStdDeviationMillis)

  /**
   * The suspicion level of the accrual failure detector.
   *
   * If a connection does not have any records in failure detector then it is
   * considered healthy.
   */
  def phi: Double = phi(clock())

  /**
   * Calculation of phi, derived from the Cumulative distribution function for
   * N(mean, stdDeviation) normal distribution, given by
   * 1.0 / (1.0 + math.exp(-y * (1.5976 + 0.070566 * y * y)))
   * where y = (x - mean) / standard_deviation
   * This is an approximation defined in β Mathematics Handbook (Logistic approximation).
   * Error is 0.00014 at +- 3.16
   * The calculated value is equivalent to -log10(1 - CDF(y))
   */
  def phi(timeDiff: Long, mean: Double, stdDeviation: Double): Double = {
    val y = (timeDiff - mean) / stdDeviation
    val e = math.exp(-y * (1.5976 + 0.070566 * y * y))
    if (timeDiff > mean)
      -math.log10(e / (1.0 + e))
    else
      -math.log10(1.0 - 1.0 / (1.0 + e))
  }

  private def phi(timestamp: Long): Double = {
    val oldTimestamp = timestamps.lastOption

    if (oldTimestamp.isEmpty) 0.0 // treat unmanaged connections, e.g. with zero heartbeats, as healthy connections
    else {
      val timeDiff = timestamp - oldTimestamp.get

      val stdDeviationP = ensureValidStdDeviation(stdDeviation)

      phi(timeDiff, mean + acceptableHeartbeatPauseMillis, stdDeviationP)
    }
  }

}
