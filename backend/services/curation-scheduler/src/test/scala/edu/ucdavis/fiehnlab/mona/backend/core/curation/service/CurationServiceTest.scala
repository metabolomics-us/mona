package edu.ucdavis.fiehnlab.mona.backend.core.curation.service

import java.io.InputStreamReader
import javax.annotation.PostConstruct

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.core.curation.CurationScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.curation.controller.CurationController
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.stereotype.Component
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by wohlg on 4/12/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[CurationScheduler]), webEnvironment = WebEnvironment.RANDOM_PORT, properties = Array("eureka.client.enabled:false"))
class CurationServiceTest extends AbstractSpringControllerTest with Eventually {

  @LocalServerPort
  private val port = 0

  @Autowired
  val testCurationRunner: TestCurationRunner = null

  @Autowired
  val notificationCounter: ReceivedEventCounter[Notification] = null

  @Autowired
  val curationService: CurationService = null

  @Autowired
  val curationController: CurationController = null

  @Autowired
  val mongoRepository: ISpectrumMongoRepositoryCustom = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CurationServiceTest" should {
    val exampleSpectrum: Spectrum = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      mongoRepository.deleteAll()
      exampleRecords.foreach(x => mongoRepository.save(x))
    }

    "scheduleSpectra" in {
      val count = notificationCounter.getEventCount
      testCurationRunner.resetMessageStatus()

      curationService.scheduleSpectrum(exampleSpectrum)

      eventually(timeout(10 seconds)) {
        assert(testCurationRunner.messageReceived)
        assert(notificationCounter.getEventCount == count + 1)
      }
    }

    "schedule all spectra via controller" in {
      (1 to 10).foreach { i =>
        logger.info(s"Test $i/10")

        val count = notificationCounter.getEventCount
        testCurationRunner.resetMessageStatus()

        curationController.curateByQuery("")

        eventually(timeout(10 seconds)) {
          assert(testCurationRunner.messageReceived)
          assert(testCurationRunner.messageCount == 58)
          assert(notificationCounter.getEventCount - count == 58)
        }
      }
    }

    "schedule spectra by query via controller" in {
      (1 to 10).foreach { i =>
        logger.info(s"Test $i/10")

        val count = notificationCounter.getEventCount
        testCurationRunner.resetMessageStatus()

        curationController.curateByQuery("metaData=q='name==\"ion mode\" and value==negative'")

        eventually(timeout(10 seconds)) {
          assert(testCurationRunner.messageReceived)
          assert(testCurationRunner.messageCount == 25)
          assert(notificationCounter.getEventCount - count == 25)
        }
      }
    }
  }
}

/**
  * simple test class to ensure the message was processed
  */
@Component
class TestCurationRunner extends GenericMessageListener[Spectrum] {

  @Autowired
  private val connectionFactory: ConnectionFactory = null

  @Autowired
  val rabbitAdmin: RabbitAdmin = null

  @Autowired
  val queue: Queue = null

  @PostConstruct
  def init(): Unit = {
    rabbitAdmin.declareQueue(queue)

    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setQueues(queue)
    container.setMessageListener(this)
    container.setRabbitAdmin(rabbitAdmin)
    container.start()
  }

  var messageReceived: Boolean = false
  var messageCount: Int = 0

  override def handleMessage(spectrum: Spectrum): Unit = {
    messageReceived = true
    messageCount += 1
  }

  def resetMessageStatus(): Unit = {
    messageReceived = false
    messageCount = 0
  }
}