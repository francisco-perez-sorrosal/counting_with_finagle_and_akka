package com.linkingenius.finagle.counter

import scala.collection.mutable.DoubleLinkedList

class ConveyorQueue private (val maxItems: Int) {

  private var queue = DoubleLinkedList[Int]()
  private var sumElems = 0

  def totalSum: Int = sumElems

  def numElems: Int = queue.size

  def shift(numShifted: Int) {
    // In case too many items shifted, just clear the queue. 
    if (numShifted >= maxItems) {
      queue = DoubleLinkedList[Int]()
      sumElems = 0
      return
    }

    // Push all the needed zeros. 
    for (i <- 1 to numShifted)
      queue = queue :+ 0

    // Let all the excess items fall off. 
    if (queue.size > maxItems) {
      val (firstElems, rest) = queue.splitAt(queue.size - maxItems)
      queue = rest
      sumElems -= (0 /: firstElems)(_ + _)
    }
  }

  def addToBack(count: Int) {
    if (queue.isEmpty) shift(1); // Make sure q has at least 1 item.
    updateLastElemInQueueWith(count)
    sumElems += count
  }

  private def updateLastElemInQueueWith(additionalValue: Int) {
    var lastValue = 0
    queue.lastOption match {
      case Some(x) => lastValue = x
      case None => lastValue = 0
    }
    queue = queue.init :+ (lastValue + additionalValue)
  }

}

object ConveyorQueue {
  def apply(maxItems: Int) = new ConveyorQueue(maxItems)
}
