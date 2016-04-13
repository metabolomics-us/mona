package edu.ucdavis.fiehnlab.mona.backend.core.curation

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import org.springframework.amqp.core.{Binding, BindingBuilder, Queue, TopicExchange}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.{Bean, Import}

/**
  * Created by wohlg on 4/12/2016.
  */
@SpringBootApplication
@EnableDiscoveryClient
@Import(Array(classOf[MonaEventBusConfiguration],classOf[MonaNotificationBusConfiguration]))
class CurrationScheduler {

  @Bean(name = Array("spectra-curration-queue"))
  def queueName:String = "curration-queue"

  @Bean
  def queue:Queue = {
    new Queue(queueName, false);
  }

  @Bean
  def exchange:TopicExchange = {
    new TopicExchange("spectra-curration");
  }

  @Bean
  def binding(queue:Queue, exchange:TopicExchange):Binding = {
    BindingBuilder.bind(queue).to(exchange).`with`(queueName);
  }
}

/**
  * our local server, which should be connecting to eureka, etc
  */
object CurrationScheduler extends App{
  new SpringApplication(classOf[CurrationScheduler]).run()

}
