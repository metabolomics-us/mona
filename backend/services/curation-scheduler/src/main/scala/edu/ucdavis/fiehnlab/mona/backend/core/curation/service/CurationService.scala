package edu.ucdavis.fiehnlab.mona.backend.core.curation.service

import java.util.Date

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Service

/**
  * Created by wohlg on 4/12/2016.
  */
@Service
class CurationService {

  @Autowired
  @Qualifier("spectra-curation-queue")
  val queueName: String = null

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  @Autowired
  val notifications: EventBus[Notification] = null

  /**
    * sends this spectrum to our dedicated queue. This queue can have many consumers to then
    * asynchronously process and curate the object
    *
    * @param spectrum
    */
  def scheduleSpectrum(spectrum: Spectrum): Unit = {
    rabbitTemplate.convertAndSend(queueName, spectrum)
    notifications.sendEvent(Event(Notification(CurationScheduled(spectrum), getClass.getName)))
  }
}

/**
  * simple event to let people who are interested in notifications know that we scheduled one
  *
  * @param spectrum
  * @param time
  */
case class CurationScheduled(spectrum: Spectrum, time: Date = new Date())
