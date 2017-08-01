package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.config

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.listener.DownloadListener
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

/**
  * Created by sajjan on 7/21/17.
  */
@Configuration
@ComponentScan(value = Array("edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner"))
class DownloadListenerConfig {

  @Autowired
  @Qualifier("spectra-download-queue")
  val queueName: String = null

  @Bean
  def container(connectionFactory: ConnectionFactory, listener: DownloadListener, messageConverter: MessageConverter): SimpleMessageListenerContainer = {
    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setMessageListener(listener)
    container.setMessageConverter(messageConverter)
    container.setQueueNames(queueName)
    container
  }
}
