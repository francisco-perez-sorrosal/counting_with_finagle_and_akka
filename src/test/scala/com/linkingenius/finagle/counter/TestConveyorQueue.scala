package com.linkingenius.finagle.counter

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.mutable.Before
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class TestConveyorQueue extends Specification {

  "An empty queue" should {
    "have 0 elems and total sum = 0" in new emptyQueueOf10ElementsContext {
      queue.numElems must equalTo(0)
      queue.totalSum must equalTo(0)
    }
    "have 1 elem when an element is added" in new emptyQueueOf10ElementsContext {
      queue.addToBack(5)
      queue.numElems must equalTo(1)
    }
    "have a total sum of 0 when the element 0 is added" in new emptyQueueOf10ElementsContext {
      queue.addToBack(0)
      queue.totalSum must equalTo(0)
    }
    "have a total sum > 0 when an element > 0 is added" in new emptyQueueOf10ElementsContext {
      queue.addToBack(10)
      queue.totalSum must be_>(0)
    }
    "have 1 elems and total sum = 10 when two elements equal to 5 are added" in new emptyQueueOf10ElementsContext {
      queue.addToBack(5)
      queue.addToBack(5)
      queue.numElems must equalTo(1)
      queue.totalSum must equalTo(10)
    }
    "have 2 elems and total sum = 10 when two elements equal to 5 are added and a shift of 1 is done in between" in new emptyQueueOf10ElementsContext {
      queue.addToBack(5)
      queue.shift(1)
      queue.addToBack(5)
      queue.numElems must equalTo(2)
      queue.totalSum must equalTo(10)
    }
    "have 5 elems and total sum = 10 when two elements equal to 5 are added and a shift of 4 is done in between" in new emptyQueueOf10ElementsContext {
      queue.addToBack(5)
      queue.shift(4)
      queue.addToBack(5)
      queue.numElems must equalTo(5)
      queue.totalSum must equalTo(10)
    }
    "have 10 elems and total sum = 10 when two elements equal to 5 are added and a shift of 9 is done in between" in new emptyQueueOf10ElementsContext {
      queue.addToBack(5)
      queue.shift(9)
      queue.addToBack(5)
      queue.numElems must equalTo(10)
      queue.totalSum must equalTo(10)
    }
    "have 1 elems and total sum = 5 when two elements equal to 5 are added and a shift of 10 is done in between" in new emptyQueueOf10ElementsContext {
      queue.addToBack(5)
      queue.shift(10)
      queue.addToBack(5)
      queue.numElems must equalTo(1)
      queue.totalSum must equalTo(5)
    }

  }

  trait emptyQueueOf10ElementsContext extends Scope {
    var queue = ConveyorQueue(10)
  }

  trait queueWithTwoElementsEqualTo5Context extends Scope {
    var queue = ConveyorQueue(10)

  }
}