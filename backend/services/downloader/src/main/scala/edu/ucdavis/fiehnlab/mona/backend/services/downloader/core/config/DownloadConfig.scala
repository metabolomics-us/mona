package edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.config

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
  * Created by sajjan on 6/6/16.
  */
@Configuration
@EnableMongoRepositories(basePackageClasses = Array(classOf[QueryExportMongoRepository], classOf[PredefinedQueryMongoRepository]))
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
