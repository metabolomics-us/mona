package edu.ucdavis.fiehnlab.mona.backend.services.downloader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.IQueryExportMongoRepository
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Import}

/**
  * Created by sajjan on 5/25/16.
  */
@SpringBootApplication
@Import(Array(classOf[MongoConfig], classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration]))
class Downloader extends LazyLogging {
  @Bean
  def queryExportMongoRepository: IQueryExportMongoRepository = null
}

object Downloader extends App {
  new SpringApplication(classOf[Downloader]).run()
}
