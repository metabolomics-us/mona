package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.config.DownloadListenerConfig
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, FilterType}


/**
  * Created by sajjan on 5/26/16.
  */
@Configuration
class TestConfig {

  @Bean
  def loginService: LoginService = new MongoLoginService
}
