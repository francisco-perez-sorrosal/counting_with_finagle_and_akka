package com.linkingenius.finagle.counter

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.mutable.Before
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import com.weiglewilczek.slf4s.Logger

@RunWith(classOf[JUnitRunner])
class TestMinuteHourCounter extends Specification {
  private val logger = Logger(this.getClass())

  "When adding a counter equal to -1 to an empty MinuteHourCounter, it" should {
    "throw an IllegalArgumentException" in new emptyCounterContext {
      logger.debug("This is the first test!!!")
      emptyCounter.add(-1) must throwAn[IllegalArgumentException]
    }
  }

  "An empty MinuteHourCounter" should {
    "have 0 elems" in new emptyCounterContext {
      emptyCounter.countElems mustEqual 0
    }
    "return 0 for the last minute count of elements" in new emptyCounterContext {
      emptyCounter.lastMinuteCount mustEqual 0
    }
    "return 0 for the last hour count of elements" in new emptyCounterContext {
      emptyCounter.lastHourCount mustEqual 0
    }
  }

  "When adding a counter equal to 10 to an empty MinuteHourCounter, it" should {
    "have 1 elem" in new emptyCounterContext {
      emptyCounter.add(10)
      emptyCounter.countElems mustEqual 1
    }
    "return 10 for the last minute count of elements" in new emptyCounterContext {
      emptyCounter.add(10)
      emptyCounter.lastMinuteCount must beEqualTo(10)
    }
    "return 10 for the last hour count of elements" in new emptyCounterContext {
      emptyCounter.add(10)
      emptyCounter.lastHourCount must beEqualTo(10)
    }
  }

  "When adding two counters (10 & 15) to an empty MinuteHourCounter, it" should {
    "have 2 elems" in new emptyCounterContext {
      emptyCounter.add(10)
      emptyCounter.add(15)
      emptyCounter.countElems mustEqual 2
    }
    "return 25 for the last minute count of elements" in new emptyCounterContext {
      emptyCounter.add(10)
      emptyCounter.add(15)
      emptyCounter.lastMinuteCount must beEqualTo(25)
    }
    "return 25 for the last hour count of elements" in new emptyCounterContext {
      emptyCounter.add(10)
      emptyCounter.add(15)
      emptyCounter.lastHourCount must beEqualTo(25)
    }
  }

  "When adding a counter equal to 10 to an empty MinuteHourCounter and wait 60 seconds, it" should {
    "return 0 for the last minute count of elements" in new emptyCounterContext {
      emptyCounter.add(10)
      Thread.sleep(10000)
      emptyCounter.lastMinuteCount must beEqualTo(0)
      emptyCounter.lastHourCount must beEqualTo(10)
    }
  }

  step {
    success
  }

  trait emptyCounterContext extends Scope {
    lazy val emptyCounter = new MinuteHourCounter()
  }

}