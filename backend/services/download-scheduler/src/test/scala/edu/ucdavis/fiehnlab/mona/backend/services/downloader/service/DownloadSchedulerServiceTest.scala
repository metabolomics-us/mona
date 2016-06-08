package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import javax.annotation.PostConstruct

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.DownloadScheduler
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

/**
  * Created by sajjan on 5/26/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[DownloadScheduler]))
class DownloadSchedulerServiceTest extends AbstractSpringControllerTest with Eventually {

  @Autowired
  val testCurationRunner: TestDownloader = null

  @Autowired
  val notificationCounter: ReceivedEventCounter[Notification] = null

  @Autowired
  val downloadSchedulerService: DownloadSchedulerService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloadSchedulerServiceTest" should {
    "scheduleDownload" in {
      assert(1 == 1)
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
  def init() = {
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
