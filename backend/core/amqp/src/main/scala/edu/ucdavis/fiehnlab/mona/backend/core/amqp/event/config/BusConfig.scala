package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter._
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * This class configures our communication BUS and the queue names should not be modified
  */
@Configuration
class BusConfig extends LazyLogging{

  @Bean
  def messageConverter:Jackson2JsonMessageConverter  = {
    logger.info("creating message converter")
    val converter = new Jackson2JsonMessageConverter
    converter.setJsonObjectMapper(MonaMapper.create)
    converter
  }

  @Bean
  def rabbitTemplate(messageConverter: MessageConverter,connectionFactory:ConnectionFactory):RabbitTemplate = {
    logger.info("creating custom rabbit template")
    val template = new RabbitTemplate(connectionFactory)
    template.setMessageConverter(messageConverter)
    template
  }

}
