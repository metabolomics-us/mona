package edu.ucdavis.fiehnlab.mona.backend.core.curation.service

import java.io.InputStreamReader
import javax.annotation.PostConstruct

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, ReceivedEventCounter}
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration, Notification}
import edu.ucdavis.fiehnlab.mona.backend.core.curation.CurrationScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.concurrent.Eventually
import org.springframework.amqp.core.{Message, MessageListener, Queue}
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.stereotype.Component
import org.springframework.test.context.{TestContextManager, TestPropertySource}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.concurrent.duration._

/**
  * Created by wohlg on 4/12/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[CurrationScheduler]))
class CurationServiceTest extends AbstractSpringControllerTest with Eventually {

  @Autowired
  val testCurrationRunner: TestCurrationRunner = null

  @Autowired
  val notificationCounter: ReceivedEventCounter[Notification] = null

  @Autowired
  val currationService: CurationService = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "CurrationServiceTest" should {

    val reader = JSONDomainReader.create[Spectrum]

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrum: Spectrum = reader.read(input)

    "scheduleSpectra" in {

      val count = notificationCounter.getEventCount

      testCurrationRunner.messageReceived = false
      currationService.scheduleSpectra(spectrum)

      eventually(timeout(10 seconds)) {
        assert(testCurrationRunner.messageReceived)
      }

      eventually(timeout(10 seconds)) {
        assert(notificationCounter.getEventCount > count)
      }
    }
  }
}

/**
  * simple test class to ensure the message was processed
  */
@Component
class TestCurrationRunner extends MessageListener {

  @Autowired
  private val connectionFactory: ConnectionFactory = null

  @Autowired
  val rabbitAdmin:RabbitAdmin = null

  @Autowired
  val queue:Queue = null

  @PostConstruct
  def init = {
    rabbitAdmin.declareQueue(queue)

    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)

    container.setQueues(queue)
    container.setMessageListener(this)
    container.setRabbitAdmin(rabbitAdmin)
    container.start()
  }

  var messageReceived: Boolean = false

  override def onMessage(message: Message): Unit = messageReceived = true
}