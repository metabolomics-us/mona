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
class CurrationService {

  @Autowired
  @Qualifier("spectra-curration-queue")
  val queueName:String = null

  @Autowired
  val rabbitTemplate:RabbitTemplate = null


  @Autowired
  val notifications: EventBus[Notification] = null

  /**
    * sends this spectrum to our dedicated queue. This queue can have many consumers to than
    * asynchrly process and curret the object
    * @param spectrum
    */
  def scheduleSpectra(spectrum:Spectrum) = {
    rabbitTemplate.convertAndSend(queueName,spectrum)
    notifications.sendEvent(Event(Notification(CurrationScheduled(spectrum), getClass.getName)))
  }

}

/**
  * simple event to let people who are interrested in notifications know that we scheduled one
  * @param spectrum
  * @param time
  */
case class CurrationScheduled(spectrum: Spectrum, time:Date = new Date())
