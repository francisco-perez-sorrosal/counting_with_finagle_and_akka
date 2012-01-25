package com.linkingenius.finagle.counter

import java.net.URI

import com.twitter.util.CountDownLatch
import com.weiglewilczek.slf4s.Logger

import akka.actor.Actor.actorOf

object WebTester {

  private val logger = Logger(getClass())

  def main(args: Array[String]) {
    if (args.length < 5) {
      println("Usage: WebTester url string_to_add times n_of_workers n_of_requests")
      exit(1)
    }

    // Extract command line arguments
    val uri = new URI(args(0))
    val stringToAdd = args(1)
    val times = args(2).toInt
    val noOfHTTPRequesters = args(3).toInt
    val totalRequests = args(4).toInt

    // Setup the condition to wait for all the HTTPRequesters
    val latch = new CountDownLatch(1)

    // Launch HTTPRequesters
    val master = actorOf(new HTTPRequesterManager(
      uri,
      stringToAdd,
      times,
      noOfHTTPRequesters,
      totalRequests,
      latch)).start()
    master ! TriggerActors

    // Wait till all the HTTPRequesters are over
    latch.await()
    println("C'est fini!")
    exit(0)
  }

}

