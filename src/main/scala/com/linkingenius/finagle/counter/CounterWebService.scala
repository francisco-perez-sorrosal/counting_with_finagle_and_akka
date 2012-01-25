package com.linkingenius.finagle.counter

import org.jboss.netty.handler.codec.http.{ HttpRequest, HttpResponse }
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.{ Http, Response }
import com.twitter.finagle.Service
import com.twitter.util.Future
import java.net.InetSocketAddress
import util.Properties
import akka.actor.Actor.actorOf
import akka.util.Duration
import akka.util.duration._
import akka.actor.{ Index => _, _ }
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8
import com.weiglewilczek.slf4s.Logger
import com.twitter.finagle.builder.Server
import com.twitter.finagle.SimpleFilter
import java.net.URI

object CounterWebService {

  val logger = Logger(getClass())

  class Counter(counter: CounterService) extends SimpleFilter[HttpRequest, HttpResponse] {

    def apply(request: HttpRequest, continue: Service[HttpRequest, HttpResponse]) = {
      val uri: URI = new URI(request.getUri())
      if (uri.getPath().equals("/compute"))
        counter.add(1)
      continue(request)
    }
  }

  class Responder(
    requestCounter: CounterService,
    bytesAppendedCounter: CounterService,
    stringAppenderTimeCounter: CounterService,
    stringBufferAppenderTimeCounter: CounterService) extends Service[HttpRequest, HttpResponse] {

    def apply(request: HttpRequest): Future[HttpResponse] = {

      val response = new DefaultHttpResponse(HTTP_1_1, OK)
      val uri: URI = new URI(request.getUri())
      val responseValue = uri.getPath() match {
        case "/compute" =>
          val string = request.getHeader("StringToAdd")
          val times = request.getHeader("TimesToAddTheString").toInt
          compute(string, times)
        case "/stats" =>
          generateStats
        case "/reset" =>
          requestCounter.reset
          bytesAppendedCounter.reset
          stringAppenderTimeCounter.reset
          stringBufferAppenderTimeCounter.reset
          generateStats
        case _ => uri.getPath() + " is not valid!!! Try with /compute or /stats"
      }
      response.setContent(copiedBuffer(responseValue, UTF_8))

      Future(response)

    }

    def compute(stringToAdd: String, times: Int): String = {
      var resultInfo: String = ""
      var start, processingTime: Long = 0

      start = System.currentTimeMillis
      var stringTestElem: String = ""
      for (i <- 1 to times)
        stringTestElem += stringToAdd
      processingTime = System.currentTimeMillis - start

      stringAppenderTimeCounter.add(processingTime.toInt)

      resultInfo = "\nTime in appending  %s %s times to a String:\t%s millis".format(stringToAdd, times, processingTime)

      start = System.currentTimeMillis
      var stringBufferTestElem: StringBuffer = new StringBuffer()
      for (i <- 1 to times)
        stringBufferTestElem.append(stringToAdd)
      processingTime = System.currentTimeMillis - start
      stringBufferAppenderTimeCounter.add(processingTime.toInt)

      resultInfo += "\nTime in appending  %s %s times to a StringBuffer:\t%s millis".format(stringToAdd, times, processingTime)

      bytesAppendedCounter.add(stringToAdd.length() * times * 2)

      resultInfo += "\nTotal bytes appended:\t%s bytes".format(stringToAdd.length() * times * 2)

      resultInfo
    }

    def generateStats(): String = {
      var resultInfo: String = "<html><body>"
      resultInfo += "This service collects several statistics for comparing the performance of Strings and StringBuffers</br>"
      resultInfo += "</br>"
      resultInfo += "<a href=\"http://localhost:8080/stats\">Stats</a>"
      resultInfo += "</br>"
      resultInfo += "<a href=\"http://localhost:8080/reset\">Reset</a>"
      resultInfo += "</br>"
      resultInfo += "<p>Number of requests processed during the last minute:\t\t\t%s (%f AVG req/seq)</br>".format(requestCounter.lastMinuteCount(), requestCounter.lastMinuteCount() / 60.toFloat)
      resultInfo += "Number of requests processed during the last hour:\t\t\t%s (%f AVG req/seq)</br>".format(requestCounter.lastHourCount(), requestCounter.lastHourCount() / 3600.toFloat)
      resultInfo += "Number of bytes appended during the last minute:\t\t\t%s</br>".format(bytesAppendedCounter.lastMinuteCount())
      resultInfo += "Number of bytes appended during the last hour:\t\t\t\t%s</br>".format(bytesAppendedCounter.lastHourCount())
      resultInfo += "Time spent processing appends to a String during the last minute:\t%s millis</br>".format(stringAppenderTimeCounter.lastMinuteCount())
      resultInfo += "Time spent processing appends to a StringBuffer during the last minute:\t%s millis</br>".format(stringBufferAppenderTimeCounter.lastMinuteCount())
      resultInfo += "Time spent processing appends to a String during the last hour:\t\t%s millis</br>".format(stringAppenderTimeCounter.lastHourCount())
      resultInfo += "Time spent processing appends to a StringBuffer during the last hour:\t%s millis</p>".format(stringBufferAppenderTimeCounter.lastHourCount())
      resultInfo += "</body></html>"
      resultInfo
    }
  }

  def main(args: Array[String]) {

    val config = TypedActorConfiguration()
      .timeout(1000 millis)
    val requestCounter = TypedActor.newInstance(classOf[CounterService], classOf[MinuteHourCounterActor], config)
    val bytesAppendedCounter = TypedActor.newInstance(classOf[CounterService], classOf[MinuteHourCounterActor], config)
    val stringAppenderTimeCounter = TypedActor.newInstance(classOf[CounterService], classOf[MinuteHourCounterActor], config)
    val stringBufferAppenderTimeCounter = TypedActor.newInstance(classOf[CounterService], classOf[MinuteHourCounterActor], config)

    val count = new Counter(requestCounter)
    val respond = new Responder(requestCounter, bytesAppendedCounter, stringAppenderTimeCounter, stringBufferAppenderTimeCounter)
    val service: Service[HttpRequest, HttpResponse] = count andThen respond

    val port = Properties.envOrElse("PORT", "8080").toInt

    val server: Server = ServerBuilder()
      .codec(Http())
      .name("my-stat-server")
      .bindTo(new InetSocketAddress(port))
      .build(service)

    logger.info("Starting on port: %s".format(port))
  }

}

