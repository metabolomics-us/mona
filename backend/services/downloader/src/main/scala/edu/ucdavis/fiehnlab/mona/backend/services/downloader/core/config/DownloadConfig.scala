package edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryRepository, QueryExportRepository}
import org.springframework.amqp.core.Queue
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import, Profile}
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
  * Created by sajjan on 6/6/16.
  */
@Configuration
@EntityScan(basePackages = Array("edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain"))
@ComponentScan
@Profile(Array("mona.persistence.downloader"))
@EnableJpaRepositories(basePackageClasses = Array(classOf[QueryExportRepository], classOf[PredefinedQueryRepository]))
class DownloadConfig {

  // Queue for individual query exports
  @Bean(name = Array("spectra-download-queue"))
  def downloadQueueName: String = "download-queue"

  @Bean(name = Array("spectra-download-queue-instance"))
  def downloadQueue: Queue = {
    new Queue(downloadQueueName, false)
  }

  // Queue for exporting predefined downloads
  @Bean(name = Array("spectra-predefined-download-queue"))
  def predefinedDownloadQueueName: String = "predefined-download-queue"

  @Bean(name = Array("spectra-predefined-download-queue-instance"))
  def predefinedDownloadQueue: Queue = {
    new Queue(predefinedDownloadQueueName, false)
  }
}
