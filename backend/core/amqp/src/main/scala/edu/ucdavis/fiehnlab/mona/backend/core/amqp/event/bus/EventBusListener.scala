package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus

import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.amqp.core.AnonymousQueue.{Base64UrlNamingStrategy, NamingStrategy}
import org.springframework.amqp.core._
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.{Jackson2JsonMessageConverter, MessageConverter}
import org.springframework.beans.factory.annotation.{Autowired, Value}

import scala.reflect._

/**
  * any instance of this class automatically receive events from the subscribed event bus. The eventbus needs to be provided
  * as constructor argument and allows this client to send messages back to it as well. But please be aware that this can easily end in
  * endless loops and should be carefully considered
  */
abstract class EventBusListener[T : ClassTag](val eventBus: EventBus[T]) extends MessageListener with LazyLogging {

  val objectMapper:ObjectMapper = MonaMapper.create

  @Autowired
  private val connectionFactory: ConnectionFactory = null

  @Autowired
  private val messageConverter: MessageConverter = null

  @Autowired
  private val rabbitAdmin:RabbitAdmin = null

  @Value("${spring.application.name:unknown}")
  private var queueName = "unknown"

  /**
    * he we define the anonymous temp queue and the fan exchange
    * system for all the applications to communicate with
    */
  @PostConstruct
  def init = {
    logger.info("configuring queue connection")

    if(queueName == "unknown"){
      queueName = new Base64UrlNamingStrategy().generateName()
    }
    else{
      queueName = s"${eventBus.busName}-${queueName}"
    }

    val queue = new Queue(queueName,false,false,true)

    val exchange = new FanoutExchange(eventBus.busName, false, true)

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
    container.setMessageConverter(messageConverter)
    container.setExclusive(true)

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
    logger.debug(s"received event at ${getClass.getSimpleName}")
    logger.debug(s"message received: ${new String(message.getBody)}")
    logger.debug(s"type of class: ${classTag[T].runtimeClass}")
    val content:Event[Any] = objectMapper.readValue(message.getBody,classTag[Event[T]].runtimeClass).asInstanceOf[Event[Any]]
    logger.debug(s"type of event is ${content.getClass.getSimpleName}")
    logger.debug(s"type of event content is ${content.content.getClass.getSimpleName}")

    val newContent:T = objectMapper.convertValue(content.content,classTag[T].runtimeClass).asInstanceOf[T]
    logger.debug(s"type of converted event content is ${newContent.getClass.getSimpleName}")

    received(Event[T](newContent,content.dateFired,content.eventType))
  }
}


/**
  * This class counts all the received events on the subscribed event bus
  */
class ReceivedEventCounter[T : ClassTag](override val eventBus: EventBus[T]) extends EventBusListener[T](eventBus) with LazyLogging {

  /**
    * atomic counter to keep track of all events
    */
  private val counter: AtomicLong = new AtomicLong()
  /**
    * reports how many events the bus has seen
    *
    * @return
    */
  def getEventCount: Long = counter.get()

  /**
    * just counts internally
    *
    * @param event
    */
  override def received(event: Event[T]): Unit = {
    counter.incrementAndGet()
  }
}