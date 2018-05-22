package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.config

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.listener.{PredefinedQueryExportListener, QueryExportListener}
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}

/**
  * Created by sajjan on 7/21/17.
  */
@Configuration
@ComponentScan(value = Array("edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner"))
@Profile(Array("docker"))
class DownloadListenerConfig {

  @Autowired
  @Qualifier("spectra-download-queue")
  val exportQueueName: String = null

  @Autowired
  @Qualifier("spectra-predefined-download-queue")
  val predefinedQueueName: String = null

  @Bean
  def container(connectionFactory: ConnectionFactory, queryExportListener: QueryExportListener,
                predefinedQueryExportListener: PredefinedQueryExportListener,
                messageConverter: MessageConverter): SimpleMessageListenerContainer = {

    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setMessageListener(queryExportListener, predefinedQueryExportListener)
    container.setMessageConverter(messageConverter)
    container.setQueueNames(exportQueueName, predefinedQueueName)
    container
  }
}
