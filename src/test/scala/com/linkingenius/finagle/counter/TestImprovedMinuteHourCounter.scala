package com.linkingenius.finagle.counter

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.mutable.Before
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import akka.actor.{ SupervisorFactory, TypedActor, Supervisor }
import akka.config.Supervision._

@RunWith(classOf[JUnitRunner])
class TestImprovedMinuteHourCounter extends Specification {

  //  "When adding a counter equal to -1 to an empty ImprovedMinuteHourCounter, it" should {
  //    "throw an IllegalArgumentException" in new emptyCounterContext {
  //
  //      // Setup the supervisorFactory with a config and a list of Actors to supervise
  //      val supervisorFactory = SupervisorFactory(
  //        SupervisorConfig(OneForOneStrategy(List(classOf[Exception]), 3, 1000), Nil))
  //      // Instantiate and start the supervisor, this also starts all supervised Actors
  //      val supervisor = supervisorFactory.newInstance
  //      supervisor.start
  //
  //      emptyCounter.add(-1) must throwAn[IllegalArgumentException]
  //
  //      supervisor.shutdown
  //    }
  //  }

  "An empty ImprovedMinuteHourCounter" should {
    "return 0 for the last minute count of elements" in new emptyCounterContext {
      emptyCounter.lastMinuteCount mustEqual 0
    }
    "return 0 for the last hour count of elements" in new emptyCounterContext {
      emptyCounter.lastHourCount mustEqual 0
    }
  }

  "When adding a counter equal to 10 to an empty ImprovedMinuteHourCounter, it" should {
    "return 10 for the last minute count of elements" in new emptyCounterContext {
      emptyCounter.add(10)
      emptyCounter.lastMinuteCount must beEqualTo(10)
    }
    "return 10 for the last hour count of elements" in new emptyCounterContext {
      emptyCounter.add(10)
      emptyCounter.lastHourCount must beEqualTo(10)
    }
  }

  "When adding two counters (10 & 15) to an empty ImprovedMinuteHourCounter, it" should {
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

  "When adding a counter equal to 10 to an empty ImprovedMinuteHourCounter and wait 60 seconds, it" should {
    "return 0 for the last minute and hour count of elements" in new emptyCounterContext {
      emptyCounter.add(10)
      Thread.sleep(60000)
      emptyCounter.lastMinuteCount must beEqualTo(0)
      emptyCounter.lastHourCount must beEqualTo(10)
    }
  }

  "When adding 2 counters equal to 10 separated by 1 sec to an empty ImprovedMinuteHourCounter, it" should {
    "return 20 for the last minute and hour count of elements" in new emptyCounterContext {
      emptyCounter.add(10)
      Thread.sleep(60000)
      emptyCounter.add(10)
      emptyCounter.lastMinuteCount must beEqualTo(20)
      emptyCounter.lastHourCount must beEqualTo(20)
    }
  }

  "When adding 2 counters equal to 10 separated by 1 sec to an empty ImprovedMinuteHourCounter and wait 60 seconds, it" should {
    "return 10 for the last minute count and 20 for the last hour count of elements" in new emptyCounterContext {
      emptyCounter.add(10)
      Thread.sleep(60000)
      emptyCounter.add(10)
      Thread.sleep(9000)
      emptyCounter.lastMinuteCount must beEqualTo(10)
      emptyCounter.lastHourCount must beEqualTo(20)
    }
  }
  "When adding 10 counters equal to 10 separated by 1 sec to an empty ImprovedMinuteHourCounter and wait 60 seconds, it" should {
    "return 90 for the last minute count and 100 for the last hour counts of elements" in new emptyCounterContext {
      for (i <- 1 to 10) {
        emptyCounter.add(10)
        Thread.sleep(60000)
      }
      emptyCounter.lastMinuteCount must beEqualTo(90)
      emptyCounter.lastHourCount must beEqualTo(100)
    }
  }

  "When adding 11 counters equal to 10 separated by 1 sec to an empty ImprovedMinuteHourCounter and wait 60 seconds, it" should {
    "return 90 for the last minute count and 110 for the last hour counts of elements" in new emptyCounterContext {
      for (i <- 1 to 11) {
        emptyCounter.add(10)
        Thread.sleep(60000)
      }
      emptyCounter.lastMinuteCount must beEqualTo(90)
      emptyCounter.lastHourCount must beEqualTo(110)
    }
  }

  "When adding 20 counters equal to 10 separated by 1 sec to an empty ImprovedMinuteHourCounter and wait 60 seconds, it" should {
    "return 90 for the last minute count and 200 for the last hour counts of elements" in new emptyCounterContext {
      for (i <- 1 to 20) {
        emptyCounter.add(10)
        Thread.sleep(60000)
      }
      emptyCounter.lastMinuteCount must beEqualTo(90)
      emptyCounter.lastHourCount must beEqualTo(200)
    }
  }

  step {
    success
  }

  trait emptyCounterContext extends Scope {
    lazy val emptyCounter = TypedActor.newInstance(classOf[CounterService], classOf[MinuteHourCounterActor], 1000)
  }

}
