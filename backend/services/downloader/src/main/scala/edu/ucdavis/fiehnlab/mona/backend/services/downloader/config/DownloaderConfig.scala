package edu.ucdavis.fiehnlab.mona.backend.services.downloader.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.IQueryExportMongoRepository
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * Created by sajjan on 5/26/16.
  */
@Configuration
class DownloaderConfig {
  @Bean(name = Array("spectra-download-queue"))
  def queryDownloadQueue: String = "download-queue"

  @Bean
  def queryExportMongoRepository: IQueryExportMongoRepository = null
}
