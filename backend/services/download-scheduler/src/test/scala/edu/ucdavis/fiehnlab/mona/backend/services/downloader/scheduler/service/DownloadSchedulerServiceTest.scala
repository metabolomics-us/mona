package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service

import java.util.Date
import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.TagStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.TagStatistics
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.{PredefinedQuery, QueryExport}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.DownloadScheduler
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.stereotype.Component
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by sajjan on 5/26/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[DownloadScheduler]), webEnvironment = WebEnvironment.DEFINED_PORT)
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

  @Autowired
  @Qualifier("tagStatisticsMongoRepository")
  val tagStatisticsRepository: TagStatisticsMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "DownloadSchedulerServiceTest" should {
    tagStatisticsRepository.deleteAll()

    "load some data" in {
      queryExportRepository.deleteAll()
      queryExportRepository.save(QueryExport("test", "test", "", "json", "test@localhost", new Date, 0, 0, null, null))

      predefinedQueryRepository.deleteAll()
      predefinedQueryRepository.save(PredefinedQuery("All Spectra", "", "", 0, null, null, null))
    }

    "scheduleDownload" in {
      val count = notificationCounter.getEventCount

      testRunner.messageReceived = false
      downloadSchedulerService.scheduleExport("", "json")

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
      downloadSchedulerService.scheduleExport("test")

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
      downloadSchedulerService.generatePredefinedExports()

      eventually(timeout(10 seconds)) {
        assert(testRunner.messageReceived)
      }

      eventually(timeout(10 seconds)) {
        assert(notificationCounter.getEventCount == count + 1)
      }
    }

    "test that library tags are translated to pregenerated downloads" in {
      tagStatisticsRepository.save(TagStatistics(null, "1 - 2 - 3", ruleBased = false, 0, "library"))

      val count = notificationCounter.getEventCount

      testRunner.messageReceived = false
      downloadSchedulerService.generatePredefinedExports()

      eventually(timeout(10 seconds)) {
        assert(testRunner.messageReceived)
      }

      eventually(timeout(10 seconds)) {
        assert(notificationCounter.getEventCount == count + 4)
      }

      assert(predefinedQueryRepository.exists("Libraries - 1"))
      assert(predefinedQueryRepository.exists("Libraries - 1 - 2"))
      assert(predefinedQueryRepository.exists("Libraries - 1 - 2 - 3"))
    }
  }
}

/**
  * Simple test class to ensure the message was processed
  */
@Component
class TestDownloader extends GenericMessageListener[Any] with LazyLogging {

  @Autowired
  private val connectionFactory: ConnectionFactory = null

  @Autowired
  val rabbitAdmin: RabbitAdmin = null

  @Autowired
  @Qualifier("spectra-download-queue-instance")
  val exportQueue: Queue = null

  @Autowired
  @Qualifier("spectra-predefined-download-queue-instance")
  val predefinedQueue: Queue = null

  @PostConstruct
  def init(): Unit = {
    rabbitAdmin.declareQueue(exportQueue)
    rabbitAdmin.declareQueue(predefinedQueue)

    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setQueues(exportQueue, predefinedQueue)
    container.setMessageListener(this)
    container.setRabbitAdmin(rabbitAdmin)
    container.start()
  }

  var messageReceived: Boolean = false
  var messageCount: Int = 0

  override def handleMessage(message: Any): Unit = {
    messageReceived = true
    messageCount += 1

    logger.debug(s"Message Received, messageCount = $messageCount")
  }

  def resetMessageStatus(): Unit = {
    messageReceived = false
    messageCount = 0
  }
}