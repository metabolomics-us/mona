package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.config

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.listener.{PredefinedQueryExportListener, QueryExportListener}
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}

/**
  * Created by sajjan on 7/21/17.
  */
@Configuration
@EnableAutoConfiguration
@ComponentScan(value = Array("edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner"))
@Profile("docker")
class DownloadListenerConfig {

  @Autowired
  @Qualifier("spectra-download-queue-instance")
  val exportQueue: Queue = null

  @Autowired
  @Qualifier("spectra-predefined-download-queue-instance")
  val predefinedQueue: Queue = null

  @Bean
  @Qualifier("message-listener-query-export")
  def queryExportListenerContainer(connectionFactory: ConnectionFactory, queryExportListener: QueryExportListener,
                messageConverter: MessageConverter): SimpleMessageListenerContainer = {

    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setMessageListener(queryExportListener)
    container.setMessageConverter(messageConverter)
    container.setQueues(exportQueue)
    container
  }

  @Bean
  @Qualifier("message-listener-predefined-query")
  def predefinedQueryListenerContainer(connectionFactory: ConnectionFactory, predefinedQueryExportListener: PredefinedQueryExportListener,
                messageConverter: MessageConverter): SimpleMessageListenerContainer = {

    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setMessageListener(predefinedQueryExportListener)
    container.setMessageConverter(messageConverter)
    container.setQueues(predefinedQueue)
    container
  }
}
