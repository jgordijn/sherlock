package io.sherlock.detector

object FailureDetector {
  def calculateUpProbability(timestamps: List[Long]): Byte = {
    val timestampsSorted = timestamps.sorted
    val first = timestampsSorted.head
    val diffs = timestampsSorted.foldLeft((Vector.empty[Long], first)) {
      case ((deltas, last), next) ⇒
        (deltas :+ (next - last), next)
    }._1.sorted.tail
    val median = if (diffs.isEmpty) 60000 else diffs(diffs.size / 2)
    val now = System.currentTimeMillis()
    val timeSinceLastHeartBeat = now - timestampsSorted.last
    Math.min(Math.max(0, 100 - 5 * (timeSinceLastHeartBeat / median)).toByte, 100).toByte
  }
}

