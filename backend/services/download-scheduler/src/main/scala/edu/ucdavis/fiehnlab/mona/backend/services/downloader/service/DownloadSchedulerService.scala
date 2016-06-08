package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import java.util.Date
import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import io.swagger.annotations.ApiModel
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 6/6/16.
  */
@Service
class DownloadSchedulerService extends LazyLogging {

  @Autowired
  @Qualifier("spectra-download-queue")
  val queueName: String = null


  @Autowired
  val rabbitTemplate: RabbitTemplate = null


  @Autowired
  val notifications: EventBus[Notification] = null

  /**
    * Sends a query to be scheduled for download to our dedicated queue
    *
    * @param query
    */
  def scheduleDownload(query: String): ScheduledDownload = {
    val downloadObject = ScheduledDownload(UUID.randomUUID.toString, query, new Date, null)

    rabbitTemplate.convertAndSend(queueName, downloadObject)
    notifications.sendEvent(Event(Notification(downloadObject, getClass.getName)))

    downloadObject
  }

  /**
    * sends this spectrum to our dedicated queue. This queue can have many consumers to then
    * asynchronously process and curate the object
    */
  def schedulePredefinedDownloads(): ScheduledDownload = {
    ScheduledDownload(UUID.randomUUID.toString, "", new Date, null)
  }
}


@ApiModel
case class ScheduledDownload(id: String,
                             query: String,
                             date: Date,
                             emailAddress: String
                            )