package com.linkingenius.finagle.counter

import akka.actor.{ Actor, PoisonPill }
import akka.actor.Actor.actorOf
import akka.routing.{ Routing, CyclicIterator }
import Routing._
import akka.dispatch.Dispatchers
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.Http
import com.twitter.util._ //{ CountDownLatch, Promise, Time, Timer, TimeFormat, Future, MapMaker, Duration }
import java.net.InetSocketAddress
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import com.twitter.finagle.{ Service, SimpleFilter }
import com.weiglewilczek.slf4s.Logger
import java.net.URI
import java.util.concurrent.TimeUnit

sealed trait WorkerMsg

case class TriggerActors(stringToAdd: String, times: Int) extends WorkerMsg
case class SendHTTPMsg(uri: URI, reqNo: Int) extends WorkerMsg
case class Result(str: String) extends WorkerMsg

/**
 * Creates all the sending HTTPRequesters and sends asynchronously all the messages.
 * Then it waits till all the resulting messages have been received.
 * This is similar to what is done in the Stress example from Finagle:
 * https://github.com/twitter/finagle/blob/master/finagle-example/src/main/scala/com/twitter/finagle/example/stress/Stress.scala
 *
 * @noOfHTTPRequesters - working in parallel to launch HTTPRequests
 * @noOfHTTPMsgsToSend - to the HTTPRequesters
 * @latch - To inform the caller about the end of the processing
 */
class HTTPRequesterManager(
  uri: URI,
  stringToAdd: String,
  times: Int,
  noOfHTTPRequesters: Int,
  noOfHTTPMsgsToSend: Int,
  latch: CountDownLatch) extends Actor {

  val logger = Logger(getClass())

  var nrOfRespReceived: Int = _
  var errors: Int = _
  var start: Long = _

  val workers = Vector.fill(noOfHTTPRequesters)(actorOf(new HTTPRequester).start())
  val router = Routing.loadBalancerActor(CyclicIterator(workers)).start()

  /**
   * The worker/actor that sends HTTP requests to the finagle counter service
   */
  class HTTPRequester extends Actor {

    private val logger = Logger(getClass())

    private val client: Service[HttpRequest, HttpResponse] = ClientBuilder()
      .codec(Http())
      .hostConnectionLimit(1)
      .hosts("localhost:8080")
      .retries(1)
      .build()

    //    .hostConnectionCoresize(10)
    //    .hostConnectionLimit(20)
    //    .hostConnectionMaxWaiters(1000)
    //    .sendBufferSize(1048576)
    //    .recvBufferSize(1048576)
    //    .timeout(Duration(10, TimeUnit.SECONDS))

    def receive = {
      // Perform request and return response to caller
      case SendHTTPMsg(uri, i) => self reply Result(performHTTPrequest(uri, i))
    }

    private def performHTTPrequest(uri: URI, reqNo: Int): String = {
      val request: HttpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getPath())
      request.addHeader("StringToAdd", stringToAdd)
      request.addHeader("TimesToAddTheString", times)

      logger.debug("Actor %s sending request %s".format(self.getUuid(), reqNo))
      val responseFuture: Future[HttpResponse] = client(request)

      val response = responseFuture.get(Duration(1, TimeUnit.SECONDS)) onSuccess { response =>
        responseFuture.get().getContent().toString(CharsetUtil.UTF_8)
      } onFailure { exception =>
        "ERROR"
      }

      response.toString
    }

    override def postStop() {
      logger.debug("Shutdown received by actor %s".format(self.getUuid()))
      client.release()
    }
  }

  override def preStart() {
    start = System.currentTimeMillis
  }

  def receive = {
    case TriggerActors =>
      for (i <- 1 to noOfHTTPMsgsToSend) router ! SendHTTPMsg(uri, i)
      router ! Broadcast(PoisonPill)
      router ! PoisonPill

    case Result(response) =>
      nrOfRespReceived += 1
      if (response.equalsIgnoreCase("ERROR"))
        errors += 1
      logger.debug("%s results received. This one is: %s".format(nrOfRespReceived, response))
      if (nrOfRespReceived == noOfHTTPMsgsToSend) {
        self.stop()
      }

  }

  override def postStop() {
    val stop = System.currentTimeMillis
    print("%s requests completed in: %sms ".format(nrOfRespReceived, (stop - start)))
    println("(%f req/sec)".format(nrOfRespReceived.toFloat / (stop - start).toFloat * 1000))
    println("%s successfully received".format(nrOfRespReceived - errors))
    println("%s errors".format(errors))
    // Tell the main program that the calculation is complete
    latch.countDown()
  }

}