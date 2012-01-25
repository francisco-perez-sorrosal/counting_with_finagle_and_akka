package com.linkingenius.finagle.counter

/**
 * Track the cumulative counts over the past minute and over the past hour.
 * Useful, for example, to track recent bandwidth usage.
 */
class MinuteHourCounter {
  import MinuteHourCounter._
  private type TimeCount = Tuple2[Time, Count]

  private var list: List[TimeCount] = Nil

  // Add a new data point (count >= 0)
  // For the next minute, lastMinuteCount() will be larger by +count. 
  // For the next hour, lastHourCount() will be larger by +count.
  def add(count: Count) {
    if (count < 0) throw new IllegalArgumentException
    list = (System.currentTimeMillis(), count) :: list
  }

  // Return the number of elements in the list.
  def countElems(): Count = {
    list.size
  }

  // Return the accumulated count over the past 10 seconds.
  def lastMinuteCount(): Count = {
    val now = System.currentTimeMillis()
    sumList(now - (10 * 1000))
  }

  // Return the accumulated count over the past 3600 seconds.
  def lastHourCount(): Count = {
    val now = System.currentTimeMillis()
    sumList(now - (3600 * 1000))
  }

  private def sumList(timeLimit: Long): Int = {
    val filteredList = list filter (_._1 > timeLimit)
    (0 /: filteredList)(_ + _._2)
  }
}

object MinuteHourCounter {
  private type Time = Long
  private type Count = Int

  implicit def intToCount(i: Int): Count = i
  //implicit def countToInt(c: Count): Int = c

}