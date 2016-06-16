package edu.ucdavis.fiehnlab.mona.backend.services.downloader.config

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.{ComponentScan, Configuration, Bean}
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
  * Created by sajjan on 6/6/16.
  */
@Configuration
@EnableMongoRepositories(basePackageClasses = Array(classOf[QueryExportMongoRepository], classOf[PredefinedQueryMongoRepository]))
class DownloadConfig {

  @Bean(name = Array("spectra-download-queue"))
  def downloadQueueName: String = "download-queue"

  @Bean(name = Array("spectra-download-queue-instance"))
  def downloadQueue: Queue = {
    new Queue(downloadQueueName, false)
  }
}
