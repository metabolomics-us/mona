package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service

import java.util.{Date, UUID}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.TagStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.{PredefinedQuery, QueryExport}
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

import scala.jdk.CollectionConverters._
import scala.collection.mutable.ArrayBuffer


/**
  * Created by sajjan on 6/6/16.
  */
@Service
class DownloadSchedulerService extends LazyLogging {

  @Autowired
  @Qualifier("spectra-download-queue")
  val exportQueueName: String = null

  @Autowired
  @Qualifier("spectra-predefined-download-queue")
  val predefinedQueueName: String = null

  @Autowired
  val queryExportRepository: QueryExportMongoRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null

  @Autowired
  @Qualifier("tagStatisticsMongoRepository")
  private val tagStatisticsRepository: TagStatisticsMongoRepository = null

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  @Autowired
  val notifications: EventBus[Notification] = null


  /**
    * Sends a query to be scheduled for export to our dedicated queue
    *
    * @param query
    * @param format
    */
  def scheduleExport(query: String, format: String): QueryExport = {
    logger.info(s"Scheduling query $query as $format")
    val download: QueryExport = QueryExport(UUID.randomUUID.toString, null, query, format, null, new Date, 0, 0, null, null)

    rabbitTemplate.convertAndSend(exportQueueName, download)
    notifications.sendEvent(Event(Notification(download, getClass.getName)))

    download
  }

  /**
    * Schedules an existing query export to be re-exported
    *
    * @param id
    * @return
    */
  def scheduleExport(id: String): QueryExport = {
    logger.info(s"Looking up query: $id")
    val download: QueryExport = queryExportRepository.findById(id).get()

    if (download != null) {
      logger.info(s"Rescheduling query: $id")
      rabbitTemplate.convertAndSend(exportQueueName, download)
      notifications.sendEvent(Event(Notification(download, getClass.getName)))
    }

    download
  }

  /**
    * Generates the downloads of all export formats for each predefined query download
    */
  def generatePredefinedExports(): Array[PredefinedQuery] = {

    // Update the list of pre-generated downloads based on libraries present in the database
    tagStatisticsRepository.findAll().asScala
      .filter(_.category == "library")
      .foreach { tag =>
        val tagComponents: Array[String] = tag.text.split(" - ")

        // Create each level of the tag if it contains separators
        // For example, a library tag of "Test - A" would create libraries "Test" and "Test - A"
        (1 to tagComponents.length).foreach { i =>
          val tagLabel: String = tagComponents.slice(0, i).mkString(" - ")

          if (predefinedQueryRepository.findByQuery(s"""tags.text=="$tagLabel"""").isEmpty) {
            logger.info(s"Creating new predefined download for ${tag.text}: $tagLabel")

            predefinedQueryRepository.save(PredefinedQuery(s"Libraries - $tagLabel", tagLabel, s"""tags.text=="$tagLabel"""", 0, null, null, null))
          }
        }
      }

    // Predefined downloads to schedule
    predefinedQueryRepository.findAll().asScala.toArray.map { predefinedQuery: PredefinedQuery =>
      rabbitTemplate.convertAndSend(predefinedQueueName, predefinedQuery)
      notifications.sendEvent(Event(Notification(predefinedQuery, getClass.getName)))
      predefinedQuery
    }
  }

  /**
    *
    */
  def generateStaticExports(): Array[QueryExport] = {
    // Hard-coded static exports for all spectra, export formats can be modified in the DownloaderService
    logger.info(s"Scheduling static export generation")
    val download: QueryExport = QueryExport(UUID.randomUUID.toString, "All Spectra", "", "static", null, new Date, 0, 0, null, null)

    rabbitTemplate.convertAndSend(exportQueueName, download)
    notifications.sendEvent(Event(Notification(download, getClass.getName)))

    Array(download)
  }

  /**
    * Schedules the generation of predefined exports once a day
    */
  @Scheduled(cron = "0 0 0 * * ?")
  private def schedulePredefinedExports(): Unit = {
    logger.info("Scheduling predefined export generation")
    generatePredefinedExports()
  }


  /**
    * Schedules the generation of static exports for all spectra (e.g. base64 png and ID exports) once a week
    */
  @Scheduled(cron = "0 0 12 ? * SUN")
  private def scheduleStaticExports(): Unit = {
    logger.info("Starting static export generation")
    generateStaticExports()
  }
}
