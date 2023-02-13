package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus


import javax.annotation.PostConstruct

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.rabbit.core.{RabbitAdmin, RabbitTemplate}
import org.springframework.beans.factory.annotation.Autowired

import scala.reflect.ClassTag

/**
  * This defines our general event bus and is utilized to provides us with application wide event handling
  * over a cluster
  *
  * Anyone can send events to this bus and every listener on this bus will receive it. For this to work you need to extend EventBusListener class with your concrete registrations
  * and the bus is limited at this point in time to use rabbitmq
  *
  * Since the event bus depends on spring, you should wire it together in a simple config file, looking something like this for the bus definition
  *
  * &#64;Bean
  * def eventBus: EventBus[Spectrum] = new EventBus[Spectrum]
  *
  * And to react to events a listener can be defined like this in the spring config
  *
  * &#64;Bean
  * def eventCounter: EventBusCounter[Spectrum] = new EventBusCounter[Spectrum]
  *
  * This particular listener is a simple counter to see how many events have been received in this particular configuration
  * since the class got initialized
  *
  * To send events over the bus, please utilize the sendEvent(....) method and provide an event.
  *
  * The default implementation, will than utilize json to send this over rabbitmq.
  */

class EventBus[T: ClassTag](val busName: String = "mona-event-bus") extends LazyLogging {

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  @Autowired
  private val rabbitAdmin: RabbitAdmin = null

  val objectMapper: ObjectMapper = MonaMapper.create

  val exchange = new FanoutExchange(busName, false, true)


  @PostConstruct
  def init(): Unit = {
    rabbitAdmin.declareExchange(exchange)
    rabbitAdmin.afterPropertiesSet()
  }

  /**
    * send the event along the bus, the retrievers should do something with it or plainly ignore it
    *
    * @param event
    */
  def sendEvent(event: Event[T]): Unit = {
    logger.debug(s"sending event to bus: ${event.content.getClass.getSimpleName}")
    rabbitTemplate.convertAndSend(busName, "", event)
    logger.debug("event sent!")
  }
}








