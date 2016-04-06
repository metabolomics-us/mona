package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config


import org.springframework.amqp.core.{Binding, BindingBuilder, Queue, TopicExchange}
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * This class configures our communication BUS and the queue names should not be modified
  */
@Configuration
class BusConfig {

  @Bean
  def queue: Queue = {
    new Queue("mona-event-bus", false)
  }

  @Bean
  def exchange: TopicExchange = {
    new TopicExchange("mona-bus-exchange")
  }

  @Bean
  def binding(queue: Queue, exchange: TopicExchange): Binding = {
    BindingBuilder.bind(queue).to(exchange).`with`("mona-event-bus");
  }
}
