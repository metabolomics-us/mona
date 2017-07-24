package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.config.DownloadConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

/**
  * Created by sajjan on 5/25/16.
  */
@SpringBootApplication
@Import(Array(classOf[DownloadConfig], classOf[MongoConfig], classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration]))
class Downloader

object Downloader extends App {
  new SpringApplication(classOf[Downloader]).run()
}
