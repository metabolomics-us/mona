package edu.ucdavis.fiehnlab.mona.backend.core.curation.service

import java.io.InputStreamReader
import javax.annotation.PostConstruct
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaNotificationBusCounterConfiguration, Notification}
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.core.curation.CurationScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.curation.controller.CurationController
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractSpringControllerTest
import org.hibernate.Hibernate
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.stereotype.Component
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by wohlg on 4/12/2016.
  */
@SpringBootTest(classes = Array(classOf[CurationScheduler], classOf[MonaNotificationBusCounterConfiguration]), webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class CurationServiceTest extends AbstractSpringControllerTest with Eventually with BeforeAndAfterEach {

  @Autowired
  val testCurationRunner: TestCurationRunner = null

  @Autowired
  val notificationCounter: ReceivedEventCounter[Notification] = null

  @Autowired
  val curationService: CurationService = null

  @Autowired
  val curationController: CurationController = null

  @Autowired
  val spectrumResultRepository: SpectrumRepository = null

  @Autowired
  private val transactionManager: PlatformTransactionManager = null

  private var transactionTemplate: TransactionTemplate = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  protected override def beforeEach() = (
    transactionTemplate = new TransactionTemplate(transactionManager)
    )

  protected override def afterEach() = (
    transactionTemplate = null
    )

  "CurationServiceTest" should {
    val exampleSpectrum: Spectrum = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      transactionTemplate.execute{ x =>
        spectrumResultRepository.deleteAll()
        Hibernate.initialize()
        x
      }

      transactionTemplate.execute{ x =>
        exampleRecords.foreach(y => spectrumResultRepository.save(y))
        Hibernate.initialize()
        x
      }

    }

    "scheduleSpectra" in {
      val count = transactionTemplate.execute{ x =>
        val z = notificationCounter.getEventCount
        Hibernate.initialize(z)
        z
      }
      testCurationRunner.resetMessageStatus()

      transactionTemplate.execute{ x =>
        curationService.scheduleSpectrum(exampleSpectrum)
        Hibernate.initialize()
        x
      }


      transactionTemplate.execute{ x =>
        eventually(timeout(80 seconds)) {
          assert(testCurationRunner.messageReceived)
          assert(notificationCounter.getEventCount == count + 1)
        }
        Hibernate.initialize()
        x
      }

    }

    "schedule all spectra via controller" in {
      (1 to 10).foreach { i =>
        logger.info(s"Test $i/10")

        val count = transactionTemplate.execute{ x =>
          val z = notificationCounter.getEventCount
          Hibernate.initialize(z)
          z
        }
        testCurationRunner.resetMessageStatus()

        transactionTemplate.execute{ x =>
          curationController.curateByQuery("")
          Hibernate.initialize()
          x
        }


        transactionTemplate.execute{ x =>
          eventually(timeout(80 seconds)) {
            assert(testCurationRunner.messageReceived)
            assert(testCurationRunner.messageCount == 59)
            assert(notificationCounter.getEventCount - count == 59)
          }
          Hibernate.initialize()
          x
        }

      }
    }

    "schedule spectra by query via controller" in {
      (1 to 10).foreach { i =>
        logger.info(s"Test $i/10")

        val count = transactionTemplate.execute { x =>
          val z = notificationCounter.getEventCount
          Hibernate.initialize(z)
          z
        }
        testCurationRunner.resetMessageStatus()

        transactionTemplate.execute{ x =>
          curationController.curateByQuery("metaData.name:'ion mode' and metaData.value:'negative'")
          Hibernate.initialize()
          x
        }


        transactionTemplate.execute{ x =>
          eventually(timeout(80 seconds)) {
            assert(testCurationRunner.messageReceived)
            assert(testCurationRunner.messageCount == 25)
            assert(notificationCounter.getEventCount - count == 25)
          }
          Hibernate.initialize()
          x
        }

      }
    }
  }
}

/**
  * simple test class to ensure the message was processed
  */
@Component
class TestCurationRunner extends GenericMessageListener[Spectrum] with LazyLogging {

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
    container.setAmqpAdmin(rabbitAdmin)
    container.start()
  }

  var messageReceived: Boolean = false
  var messageCount: Int = 0

  override def handleMessage(spectrum: Spectrum): Unit = {
    messageReceived = true
    messageCount += 1

    logger.debug(s"Message Received, messageCount = $messageCount")
  }

  def resetMessageStatus(): Unit = {
    messageReceived = false
    messageCount = 0
  }
}
