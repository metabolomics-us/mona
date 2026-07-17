package edu.ucdavis.fiehnlab.mona.backend.curation.config

import org.springframework.amqp.core._
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}

import scala.jdk.CollectionConverters._

/**
  * Declares the dedicated ClassyFire classification queues, kept separate from the main curation workflow so
  * classification can run on its own single consumer (the global rate limiter) without bottlenecking curation.
  *
  * Two durable queues are declared:
  *   - classyfire-queue: spectra waiting to be classified
  *   - classyfire-pending-queue: spectra whose async ClassyFire query was scheduled and is being polled. It
  *     has a message TTL and dead-letters expired messages back onto classyfire-queue, giving us a plain
  *     RabbitMQ delayed re-enqueue (no delayed message plugin required)
  */
@Configuration
class ClassyfireQueueConfig {

  @Bean(name = Array("classyfire-queue"))
  def classyfireQueueName: String = "classyfire-queue"

  @Bean(name = Array("classyfire-pending-queue"))
  def classyfirePendingQueueName: String = "classyfire-pending-queue"

  // How long a scheduled query waits before it is re-polled
  @Value("${mona.classyfire.pending.ttl:60000}")
  val pendingTtlMs: Int = 60000

  def classyfireExchangeName: String = "spectra-classyfire"

  @Bean(name = Array("classyfire-queue-instance"))
  def classyfireQueue: Queue = new Queue(classyfireQueueName, true)

  /**
    * Durable pending queue. Expired messages dead-letter through the classyfire exchange back onto the main
    * queue so the scheduled query is polled again
    *
    * @return
    */
  @Bean(name = Array("classyfire-pending-queue-instance"))
  def classyfirePendingQueue: Queue = {
    val args: Map[String, AnyRef] = Map(
      "x-message-ttl" -> Integer.valueOf(pendingTtlMs),
      "x-dead-letter-exchange" -> classyfireExchangeName,
      "x-dead-letter-routing-key" -> classyfireQueueName
    )
    new Queue(classyfirePendingQueueName, true, false, false, args.asJava)
  }

  @Bean
  def classyfireExchange: DirectExchange = new DirectExchange(classyfireExchangeName)

  /**
    * Binds classyfire-queue to the classyfire exchange so dead-lettered pending messages route back to it.
    * References the bean methods directly rather than injecting by type to avoid Queue/DirectExchange
    * ambiguity with the curation queue beans
    *
    * @return
    */
  @Bean
  def classyfireBinding: Binding = {
    BindingBuilder.bind(classyfireQueue).to(classyfireExchange).`with`(classyfireQueueName)
  }
}
