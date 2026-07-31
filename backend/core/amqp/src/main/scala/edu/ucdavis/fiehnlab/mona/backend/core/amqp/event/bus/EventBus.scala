package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus


import javax.annotation.PostConstruct

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.amqp.core.{Declarable, FanoutExchange}
import org.springframework.amqp.rabbit.core.{RabbitAdmin, RabbitTemplate}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory

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

  @Autowired
  private val beanFactory: ConfigurableListableBeanFactory = null

  val objectMapper: ObjectMapper = MonaMapper.create

  /**
    * Durable so a broker restart cannot drop the exchange, and not auto-delete so it also survives its last
    * binding going away when every listener disconnects.
    */
  val exchange = new FanoutExchange(busName, true, false)


  @PostConstruct
  def init(): Unit = {
    rabbitAdmin.declareExchange(exchange)
    BusDeclarations.register(beanFactory, s"$busName-exchange", exchange)
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

/**
  * The bus builds its exchange, queues and bindings at runtime rather than as configuration beans, since a
  * listener only knows its queue name once the application name has been resolved. 
  *
  * Registering each declaration as a singleton bean puts it in the set RabbitAdmin walks in initialize(),
  * which its own ConnectionListener runs on every connection creation. 
  */
object BusDeclarations {

  /**
    * Register a declaration under the given bean name, ignoring a name that is already taken so a second
    * bus or listener sharing a name cannot fail the context
    *
    * @param beanFactory
    * @param name
    * @param declarable
    */
  def register(beanFactory: ConfigurableListableBeanFactory, name: String, declarable: Declarable): Unit = {
    if (!beanFactory.containsSingleton(name)) {
      beanFactory.registerSingleton(name, declarable)
    }
  }
}








