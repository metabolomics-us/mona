package edu.ucdavis.fiehnlab.mona.backend.services.downloader.config

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.BusConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.IQueryExportMongoRepository
import org.springframework.context.annotation.{Import, ComponentScan, Configuration, Bean}

/**
  * Created by sajjan on 5/26/16.
  */
@Configuration
class QueryDownloaderConfig {
  @Bean(name = Array("spectra-query-download-queue"))
  def queryDownloadQueue: String = "query-download-queue"

  @Bean
  def queryExportMongoRepository: IQueryExportMongoRepository = null

  @Bean
  def loginService: LoginService = new MongoLoginService
}
