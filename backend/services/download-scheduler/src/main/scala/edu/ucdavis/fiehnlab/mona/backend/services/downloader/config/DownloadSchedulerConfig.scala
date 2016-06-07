package edu.ucdavis.fiehnlab.mona.backend.services.downloader.config

import org.springframework.context.annotation.{Bean, Configuration}

/**
  * Created by sajjan on 5/26/16.
  */
@Configuration
class DownloadSchedulerConfig {
  @Bean(name = Array("spectra-download-queue"))
  def queryDownloadQueue: String = "download-queue"
}
