package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import java.util.{Date, UUID}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.PredefinedQueryMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.{QueryExport, PredefinedQuery}
import io.swagger.annotations.ApiModel
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer


/**
  * Created by sajjan on 6/6/16.
  */
@Service
class DownloadSchedulerService extends InitializingBean with LazyLogging {

  @Autowired
  @Qualifier("spectra-download-queue")
  val queueName: String = null

  @Autowired
  val predefinedQueryMongoRepository: PredefinedQueryMongoRepository = null


  @Autowired
  val rabbitTemplate: RabbitTemplate = null


  @Autowired
  val notifications: EventBus[Notification] = null


  def afterPropertiesSet() {
    if (predefinedQueryMongoRepository.count == 0) {
      predefinedQueryMongoRepository.save(PredefinedQuery("All Spectra", "All Spectra", "", 0, null, null))
    }
  }

  /**
    * Sends a query to be scheduled for download to our dedicated queue
    *
    * @param query
    * @param format
    */
  def scheduleDownload(query: String, format: String): QueryExport = {
    val download = QueryExport(UUID.randomUUID.toString, null, query, format, null, new Date, 0, 0, null, null)

    rabbitTemplate.convertAndSend(queueName, download)
    notifications.sendEvent(Event(Notification(download, getClass.getName)))

    download
  }

  /**
    * sends this spectrum to our dedicated queue. This queue can have many consumers to then
    * asynchronously process and curate the object
    */
  def schedulePredefinedDownloads(): Array[QueryExport] = {
    val downloads: ArrayBuffer[QueryExport] = ArrayBuffer()

    predefinedQueryMongoRepository.findAll().asScala.foreach { predefinedQuery: PredefinedQuery =>
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
}
