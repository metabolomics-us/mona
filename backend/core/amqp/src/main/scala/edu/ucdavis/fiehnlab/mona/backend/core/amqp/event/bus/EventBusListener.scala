package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus

import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events.Event
import org.springframework.amqp.core._
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.{JsonMessageConverter, MessageConverter}
import org.springframework.beans.factory.annotation.Autowired

/**
  * any instance of this class automatically receive events and should ensure that
  * they utilize is to react to them
  */
abstract class EventBusListener[T] extends MessageListener with LazyLogging {

  @Autowired
  private val connectionFactory: ConnectionFactory = null

  @Autowired
  private val messageConverter:MessageConverter = null

  /**
    * he we define the anomynous temp queue and the fan exchange
    */
  @PostConstruct
  def init = {
    logger.info("configuring queue connection")
    val rabbitAdmin = new RabbitAdmin(connectionFactory)
    val queue = new AnonymousQueue()
    val exchange = new FanoutExchange("mona-event-bus", true, false)

    rabbitAdmin.declareQueue(queue)
    rabbitAdmin.declareExchange(exchange)
    rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange))
    rabbitAdmin.afterPropertiesSet()

    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setQueues(queue)
    container.setMessageListener(this)
    container.setRabbitAdmin(rabbitAdmin)
    container.setMessageConverter(messageConverter)

    logger.info("starting container")
    container.start()

  }

  /**
    * an element has been received from the bus and should be now processed
    *
    * @param event
    */
  def received(event: Event[T]): Unit

  /**
    * receives and converts the message for us
    * @param message
    */
  final override def onMessage(message: Message): Unit = {
    logger.debug(s"message received: ${new String(message.getBody)}")
    received(messageConverter.fromMessage(message).asInstanceOf[Event[T]])
  }
}
