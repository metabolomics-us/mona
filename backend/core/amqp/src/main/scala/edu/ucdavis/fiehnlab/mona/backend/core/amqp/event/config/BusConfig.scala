package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, ReceivedEventCounter}
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.converter.MonaMessageConverter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import org.springframework.amqp.core.ReturnedMessage
import org.springframework.amqp.rabbit.connection.{CachingConnectionFactory, ConnectionFactory}
import org.springframework.amqp.rabbit.core.{RabbitAdmin, RabbitTemplate}
import org.springframework.amqp.support.converter._
import org.springframework.context.annotation.{Bean, Configuration, Import, Primary}

/**
  * This class configures our communication BUS and the queue names should not be modified
  */
@Configuration
@Import(Array(classOf[DomainConfig]))
class BusConfig extends LazyLogging {

  @Bean
  val connectionFactory: ConnectionFactory = null

  @Bean
  def rabbitAdmin(connectionFactory: ConnectionFactory): RabbitAdmin = {
    new RabbitAdmin(connectionFactory)
  }

  @Bean
  @Primary
  def messageConverter: MessageConverter = {
    logger.info("creating message converter")
    new MonaMessageConverter
  }

  @Bean
  def rabbitTemplate(jsonConverter: MessageConverter, connectionFactory: ConnectionFactory): RabbitTemplate = {
    logger.info("creating custom rabbit template")

    connectionFactory match {
      case caching: CachingConnectionFactory => caching.setPublisherReturns(true)
      case _ => logger.warn("connection factory does not support publisher returns, undeliverable messages will not be reported")
    }

    val template = new RabbitTemplate(connectionFactory)
    template.setMessageConverter(jsonConverter)

    template.setMandatory(true)
    template.setReturnsCallback(new RabbitTemplate.ReturnsCallback {
      override def returnedMessage(returned: ReturnedMessage): Unit = {
        // The notification bus has no subscriber outside tests, so nothing routing off it is expected
        // rather than a lost event, and reporting it would bury the cases that matter
        if (returned.getExchange != BusConfig.NotificationBusName) {
          logger.error(s"message reached no queue, exchange '${returned.getExchange}' " +
            s"routing key '${returned.getRoutingKey}': ${returned.getReplyText}")
        }
      }
    })

    template
  }
}

object BusConfig {

  /**
    * Named here rather than inline so the notification bus and the returns callback that has to recognise it
    * cannot drift apart
    */
  val NotificationBusName: String = "mona-notification-bus"
}

/**
  * this is a specific configuration to provide us with an event bus for spectra
  * for each spring class containing this configuration
  */
@Import(Array(classOf[BusConfig]))
@Configuration
class MonaEventBusConfiguration {

  /**
    * required to notify the main event bus about spectra being modified events
    * and should ensure that all parts of the cluster stay synchronized
    *
    * @return
    */
  @Bean
  def eventBus: EventBus[Spectrum] = new EventBus[Spectrum]("mona-persistence-events")
}

@Import(Array(classOf[BusConfig], classOf[MonaEventBusConfiguration]))
@Configuration
class MonaEventBusCounterConfiguration {

  /**
    * counts all the events send over the event bus
    * which could be used later for metrics
    *
    * @return
    */
  @Bean
  def eventCounter(eventBus: EventBus[Spectrum]): ReceivedEventCounter[Spectrum] = new ReceivedEventCounter[Spectrum](eventBus)
}

/**
  * this configuration provides us with a notification bus, which forgets it's content after 60 seconds
  * for any application which would like to listen to notification events
  */
@Import(Array(classOf[BusConfig]))
@Configuration
class MonaNotificationBusConfiguration {

  @Bean
  def notificationsBus: EventBus[Notification] = new EventBus[Notification](BusConfig.NotificationBusName)
}

@Import(Array(classOf[BusConfig], classOf[MonaNotificationBusConfiguration]))
@Configuration
class MonaNotificationBusCounterConfiguration {

  /**
    * counts all the events send over the event bus
    * which could be used later for metrics
    *
    * @return
    */
  @Bean
  def notificationBusCounter(eventBus: EventBus[Notification]): ReceivedEventCounter[Notification] = new ReceivedEventCounter[Notification](eventBus)
}


/**
  * defines a notification event and is used to notify other objects on the subscribed bus that stuff is
  * happening and if they want they can react to it it or not
  *
  * @param value
  * @param origin
  */
case class Notification(value: Any, origin: String)
