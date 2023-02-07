package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.config.DownloadConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Import, Profile}

/**
  * Created by sajjan on 5/25/16.
  */
@SpringBootApplication
@Profile(Array("mona.persistence.downloader"))
@Import(Array(classOf[DownloadConfig], classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration]))
class Downloader

object Downloader extends App {
  val app = new SpringApplication(classOf[Downloader])
  app.setAdditionalProfiles("mona.persistence")
  app.setAdditionalProfiles("mona.persistence.downloader")
  app.run()
}
