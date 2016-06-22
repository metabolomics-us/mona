package edu.ucdavis.fiehnlab.mona.backend.services.downloader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.listener.DownloadListener
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.QueryExportMongoRepository
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.{Qualifier, Autowired}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Import}

/**
  * Created by sajjan on 5/25/16.
  */
@SpringBootApplication
@Import(Array(classOf[MongoConfig], classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration]))
class Downloader extends LazyLogging {

  @Autowired
  @Qualifier("spectra-download-queue")
  val queueName: String = null

  @Bean
  val queryExportMongoRepository: QueryExportMongoRepository = null

  @Bean
  val downloadListener: DownloadListener = new DownloadListener

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

object Downloader extends App {
  new SpringApplication(classOf[Downloader]).run()
}
