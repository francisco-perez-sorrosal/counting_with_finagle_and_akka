package com.linkingenius.finagle.counter
import com.weiglewilczek.slf4s.Logger

class TrailingBucketCounter private (numBuckets: Int, secsPerBucket: Int) {

  private val logger = Logger(this.getClass())

  private val buckets = ConveyorQueue(numBuckets)
  private var lastUpdateTime: Long = 0

  def add(count: Int, now: Long) {
    update(now)
    buckets.addToBack(count)
  }

  def trailingCount(now: Long): Int = {
    update(now)
    buckets.totalSum
  }

  def reset {
    buckets.shift(numBuckets + 1)
  }

  private def update(now: Long) {
    val currentBucket = now / (secsPerBucket * 1000)
    logger.debug("Current bucket: %s".format(currentBucket))
    val lastUpdateBucket = lastUpdateTime / (secsPerBucket * 1000)
    logger.debug("LastBucketUpdated: %s".format(lastUpdateBucket))
    val shift = (currentBucket - lastUpdateBucket).toInt
    logger.debug("Shift: %s".format(shift))
    buckets.shift(shift)
    lastUpdateTime = now
  }

}

object TrailingBucketCounter {
  def apply(numBuckets: Int, secsPerBucket: Int) = new TrailingBucketCounter(numBuckets, secsPerBucket)
}
