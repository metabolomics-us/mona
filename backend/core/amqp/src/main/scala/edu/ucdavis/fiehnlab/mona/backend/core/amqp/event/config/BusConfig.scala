package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, ReceivedEventCounter}
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.converter.MonaMessageConverter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.SearchTableRepository.SparseSearchTable
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import org.springframework.amqp.rabbit.connection.ConnectionFactory
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
    val template = new RabbitTemplate(connectionFactory)
    template.setMessageConverter(jsonConverter)
    template
  }
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
  def eventBus: EventBus[SpectrumResult] = new EventBus[SpectrumResult]("mona-persistence-events")

  @Bean
  def eventBusSparse: EventBus[SparseSearchTable] = new EventBus[SparseSearchTable]("mona-persistence-events-sparse")
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
  def eventCounter(eventBus: EventBus[SpectrumResult]): ReceivedEventCounter[SpectrumResult] = new ReceivedEventCounter[SpectrumResult](eventBus)

  @Bean
  def eventCounterSparse(eventBus: EventBus[SparseSearchTable]): ReceivedEventCounter[SparseSearchTable] = new ReceivedEventCounter[SparseSearchTable](eventBus)
}

/**
  * this configuration provides us with a notification bus, which forgets it's content after 60 seconds
  * for any application which would like to listen to notification events
  */
@Import(Array(classOf[BusConfig]))
@Configuration
class MonaNotificationBusConfiguration {

  @Bean
  def notificationsBus: EventBus[Notification] = new EventBus[Notification]("mona-notification-bus")
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
