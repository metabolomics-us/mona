package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired

/**
  * This defines our general event bus and is utilized to provides us with application wide event handling
  * over a cluster
  *
  * Anyone can send events to this bus and every listener on this bus will receive it. For this to work you need to extend EventBusListener class with your concrete registrations
  * and the bus is limited at this point in time to use rabbitmq
  *
  * Since the event bus depends on spring, you should wire it together in a simple config file, looking something like this for the bus definition
  *
  *   @Bean
  *   def eventBus: EventBus[Spectrum] = new EventBus[Spectrum]
  *
  *   And to react to events a listener can be defined like this in the spring config
  *
  *   @Bean
  *   def eventCounter: EventBusCounter[Spectrum] = new EventBusCounter[Spectrum]
  *
  *   This particular listener is a simple counter to see how many events have been received in this particular configuration
  *   since the class got initialized
  *
  *   To send events over the bus, please utilize the sendEvent(....) method and provide an event.
  *
  *   The default implementation, will than utilize json to send this over rabbitmq.
  */

class EventBus[T](val busName:String = "mona-event-bus") extends LazyLogging{

  @Autowired
  val rabbitTemplate:RabbitTemplate = null

  /**
    * sned the event along the bus, the retrievers should do something with it or plainly ignore it
    *
    * @param event
    */
  def sendEvent(event:Event[T]) : Unit = {
    logger.info(s"sending event to bus ${event}")
    rabbitTemplate.convertAndSend(busName,"",event)
  }
}








