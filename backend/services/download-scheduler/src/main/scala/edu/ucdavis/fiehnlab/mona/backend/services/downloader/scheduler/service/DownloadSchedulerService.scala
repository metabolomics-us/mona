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

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer


/**
  * Created by sajjan on 6/6/16.
  */
@Service
class DownloadSchedulerService extends LazyLogging {

  @Autowired
  @Qualifier("spectra-download-queue")
  val queueName: String = null

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
    * Sends a query to be scheduled for download to our dedicated queue
    *
    * @param query
    * @param format
    */
  def scheduleDownload(query: String, format: String): QueryExport = {
    logger.info(s"Scheduling query $query as $format")
    val download: QueryExport = QueryExport(UUID.randomUUID.toString, null, query, format, null, new Date, 0, 0, null, null)

    rabbitTemplate.convertAndSend(queueName, download)
    notifications.sendEvent(Event(Notification(download, getClass.getName)))

    download
  }

  /**
    * Schedules an existing query export to be re-downloaded
    *
    * @param id
    * @return
    */
  def scheduleDownload(id: String): QueryExport = {
    logger.info(s"Looking up query: $id")
    val download: QueryExport = queryExportRepository.findOne(id)

    if (download != null) {
      logger.info(s"Rescheduling query: $id")
      rabbitTemplate.convertAndSend(queueName, download)
      notifications.sendEvent(Event(Notification(download, getClass.getName)))
    }

    download
  }

  /**
    * Generates the downloads of all export formats for each predefined query download
    */
  def generatePredefinedDownloads(): Array[QueryExport] = {

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

            predefinedQueryRepository.save(PredefinedQuery(s"Libraries - $tagLabel", tagLabel, s"""tags.text=="$tagLabel"""", 0, null, null))
          }
        }
      }


    // Compile a list of downloads to schedule
    val downloads: ArrayBuffer[QueryExport] = ArrayBuffer()

    predefinedQueryRepository.findAll().asScala.foreach { predefinedQuery: PredefinedQuery =>
      if (predefinedQuery.jsonExport == null) {
        downloads.append(QueryExport(UUID.randomUUID.toString, predefinedQuery.label, predefinedQuery.query, "json", null, new Date, 0, 0, null, null))
      } else {
        downloads.append(predefinedQuery.jsonExport)
      }

      if (predefinedQuery.mspExport == null) {
        downloads.append(QueryExport(UUID.randomUUID.toString, predefinedQuery.label, predefinedQuery.query, "msp", null, new Date, 0, 0, null, null))
      } else {
        downloads.append(predefinedQuery.mspExport)
      }
    }

    // Send downloads to the queue
    downloads.foreach { download =>
      rabbitTemplate.convertAndSend(queueName, download)
      notifications.sendEvent(Event(Notification(download, getClass.getName)))
    }

    downloads.toArray
  }

  /**
    * Schedules the generation of predefined downloads once a day
    */
  @Scheduled(cron = "0 0 1 * * *")
  private def schedulePredefinedDownloads(): Unit = {
    generatePredefinedDownloads()
  }
}
