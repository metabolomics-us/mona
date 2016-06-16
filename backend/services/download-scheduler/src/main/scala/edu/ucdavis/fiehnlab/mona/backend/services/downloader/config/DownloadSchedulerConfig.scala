package edu.ucdavis.fiehnlab.mona.backend.services.downloader.config

import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * Created by sajjan on 5/26/16.
  */
@Configuration
class DownloadSchedulerConfig {

  @Bean(name = Array("spectra-download-queue"))
  def downloadQueueName: String = "download-queue"

  @Bean(name = Array("spectra-download-queue-instance"))
  def downloadQueue: Queue = {
    new Queue(downloadQueueName, false)
  }
}
