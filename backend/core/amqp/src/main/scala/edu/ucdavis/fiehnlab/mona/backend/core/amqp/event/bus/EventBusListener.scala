package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus

import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import org.springframework.amqp.core._
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.{Jackson2JsonMessageConverter, MessageConverter}
import org.springframework.beans.factory.annotation.Autowired

/**
  * any instance of this class automatically receive events from the subscribed event bus. The eventbus needs to be provided
  * as constructor argument and allows this client to send messages back to it as well. But please be aware that this can easily end in
  * endless loops and should be carefully considered
  */
abstract class EventBusListener[T](val eventBus: EventBus[T]) extends MessageListener with LazyLogging {


  @Autowired
  private val connectionFactory: ConnectionFactory = null

  @Autowired
  private val messageConverter: MessageConverter = null

  @Autowired
  private val rabbitAdmin:RabbitAdmin = null

  /**
    * he we define the anonymous temp queue and the fan exchange
    * system for all the applications to communicate with
    */
  @PostConstruct
  def init = {
    logger.info("configuring queue connection")

    val queue = new AnonymousQueue()
    val exchange = new FanoutExchange(eventBus.busName, true, false)

    rabbitAdmin.declareQueue(queue)
    rabbitAdmin.declareExchange(exchange)
    rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange))
    rabbitAdmin.afterPropertiesSet()

    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    logger.info(s"connecting to queue: ${queue.getName}")
    container.setQueues(queue)
    container.setMessageListener(this)
    container.setRabbitAdmin(rabbitAdmin)
    //container.setMessageConverter(messageConverter)

 //   logger.info(s"utilizing message converter: ${messageConverter}")

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
    *
    * @param message
    */
  final override def onMessage(message: Message): Unit = {
    logger.debug(s"message received: ${new String(message.getBody)}")
    received(messageConverter.fromMessage(message).asInstanceOf[Event[T]])
  }
}


/**
  * This class counts all the received events on the subscribed event bus
  */
class ReceivedEventCounter[T](override val eventBus: EventBus[T]) extends EventBusListener[T](eventBus) with LazyLogging {

  /**
    * atomic counter to keep track of all events
    */
  private var counter: Long = 0

  private val lock:Semaphore = new Semaphore(1)
  /**
    * reports how many events the bus has seen
    *
    * @return
    */
  def getEventCount: Long = counter

  /**
    * just counts internally
    *
    * @param event
    */
  override def received(event: Event[T]): Unit = {
    logger.debug(s"found event on bus of type ${event.content.getClass}")
    lock.acquire(1)
    counter = counter + 1
    lock.release(1)
  }
}