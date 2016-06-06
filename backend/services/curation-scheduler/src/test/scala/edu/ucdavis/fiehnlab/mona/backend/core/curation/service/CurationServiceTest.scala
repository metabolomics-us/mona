package edu.ucdavis.fiehnlab.mona.backend.core.curation.service

import java.io.InputStreamReader
import javax.annotation.PostConstruct

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.curation.CurationScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
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

/**
  * Created by wohlg on 4/12/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[CurationScheduler]))
class CurationServiceTest extends AbstractSpringControllerTest with Eventually {

  @Autowired
  val testCurationRunner: TestCurationRunner = null

  @Autowired
  val notificationCounter: ReceivedEventCounter[Notification] = null

  @Autowired
  val curationService: CurationService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CurationServiceTest" should {

    val reader = JSONDomainReader.create[Spectrum]

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrum: Spectrum = reader.read(input)

    "scheduleSpectra" in {
      val count = notificationCounter.getEventCount

      testCurationRunner.messageReceived = false
      curationService.scheduleSpectra(spectrum)

      eventually(timeout(10 seconds)) {
        assert(testCurationRunner.messageReceived)
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
class TestCurationRunner extends MessageListener {

  @Autowired
  private val connectionFactory: ConnectionFactory = null

  @Autowired
  val rabbitAdmin: RabbitAdmin = null

  @Autowired
  val queue: Queue = null

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