package com.linkingenius.finagle.counter
import akka.actor.TypedActor

trait CounterService {
  def add(count: Int)
  def lastMinuteCount(): Int
  def lastHourCount(): Int
  def reset
}

class MinuteHourCounterActor extends TypedActor with CounterService {
  private val minuteCounts = TrailingBucketCounter( /* num buckets */ 60, /* secs per bucket */ 1)
  private val hourCounts = TrailingBucketCounter( /* num buckets */ 60, /* secs per bucket */ 60)

  def add(count: Int) {
    if (count < 0) throw new IllegalArgumentException
    val now = System.currentTimeMillis
    minuteCounts.add(count, now)
    hourCounts.add(count, now)
  }

  def lastMinuteCount(): Int = {
    minuteCounts.trailingCount(System.currentTimeMillis)
  }

  def lastHourCount(): Int = {
    hourCounts.trailingCount(System.currentTimeMillis)
  }

  def reset {
    minuteCounts.reset
    hourCounts.reset
  }

}