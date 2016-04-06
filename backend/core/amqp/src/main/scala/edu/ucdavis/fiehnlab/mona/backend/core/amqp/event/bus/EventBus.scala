package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events.Event
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired

/**
  * This defines our general event bus and is utilized to provides us with application wide event handling
  * over a cluster
  */

class EventBus[T] extends LazyLogging{

  @Autowired
  val rabbitTemplate:RabbitTemplate = null

  /**
    * sned the event along the bus, the retrievers should do something with it or plainly ignore it
    *
    * @param event
    */
  def sendEvent(event:Event[T]) : Unit = {
    logger.debug(s"sending event to bus ${event}")
    rabbitTemplate.convertAndSend("mona-event-bus","",event)
  }
}








