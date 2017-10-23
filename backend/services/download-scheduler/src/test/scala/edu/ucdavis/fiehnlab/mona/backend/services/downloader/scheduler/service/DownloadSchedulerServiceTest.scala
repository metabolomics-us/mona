package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service

import java.util.Date
import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.{PredefinedQuery, QueryExport}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.DownloadScheduler
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.stereotype.Component
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by sajjan on 5/26/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[DownloadScheduler]))
class DownloadSchedulerServiceTest extends AbstractSpringControllerTest with Eventually {

  @Autowired
  val testRunner: TestDownloader = null

  @Autowired
  val notificationCounter: ReceivedEventCounter[Notification] = null

  @Autowired
  val downloadSchedulerService: DownloadSchedulerService = null

  @Autowired
  val queryExportRepository: QueryExportMongoRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "DownloadSchedulerServiceTest" should {
    "load some data" in {
      queryExportRepository.deleteAll()
      queryExportRepository.save(QueryExport("test", "test", "", "json", "test@localhost", new Date, 0, 0, null, null))

      predefinedQueryRepository.deleteAll()
      predefinedQueryRepository.save(PredefinedQuery("All Spectra", "", "", 0, null, null))
    }

    "scheduleDownload" in {
      val count = notificationCounter.getEventCount

      testRunner.messageReceived = false
      downloadSchedulerService.scheduleDownload("", "json")

      eventually(timeout(10 seconds)) {
        assert(testRunner.messageReceived)
      }

      eventually(timeout(10 seconds)) {
        assert(notificationCounter.getEventCount == count + 1)
      }
    }

    "rescheduleDownload" in {
      val count = notificationCounter.getEventCount

      testRunner.messageReceived = false
      downloadSchedulerService.scheduleDownload("test")

      eventually(timeout(10 seconds)) {
        assert(testRunner.messageReceived)
      }

      eventually(timeout(10 seconds)) {
        assert(notificationCounter.getEventCount == count + 1)
      }
    }

    "generatePredefinedDownloads" in {
      val count = notificationCounter.getEventCount

      testRunner.messageReceived = false
      downloadSchedulerService.generatePredefinedDownloads()

      eventually(timeout(10 seconds)) {
        assert(testRunner.messageReceived)
      }

      eventually(timeout(10 seconds)) {
        assert(notificationCounter.getEventCount == count + 2)
      }
    }
  }
}

/**
  * Simple test class to ensure the message was processed
  */
@Component
class TestDownloader extends GenericMessageListener[QueryExport] with LazyLogging {

  @Autowired
  private val connectionFactory: ConnectionFactory = null

  @Autowired
  val rabbitAdmin: RabbitAdmin = null

  @Autowired
  val downloadQueue: Queue = null

  @PostConstruct
  def init(): Unit = {
    rabbitAdmin.declareQueue(downloadQueue)

    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)

    container.setQueues(downloadQueue)
    container.setMessageListener(this)
    container.setRabbitAdmin(rabbitAdmin)
    container.start()
  }

  var messageReceived: Boolean = false
  var messageCount: Int = 0

  override def handleMessage(spectrum: QueryExport): Unit = {
    messageReceived = true
    messageCount += 1
    logger.debug(s"Message Received, messageCount = $messageCount")
  }

  def resetMessageStatus(): Unit = {
    messageReceived = false
    messageCount = 0
  }
}