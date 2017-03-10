package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import javax.annotation.PostConstruct

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.DownloadScheduler
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.PredefinedQueryMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.PredefinedQuery
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.amqp.core.{Message, MessageListener, Queue}
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
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloadSchedulerServiceTest" should {
    "load some data" in {
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
class TestDownloader extends MessageListener {

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

  override def onMessage(message: Message): Unit = messageReceived = true
}
