package edu.ucdavis.fiehnlab.mona.backend.services.downloader.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}


/**
  * Created by sajjan on 5/26/16.
  */
@Configuration
@ComponentScan(Array("edu/ucdavis/fiehnlab/mona/backend/services/downloader"))
class TestConfig {

  @Bean
  def loginService: LoginService = new MongoLoginService
}
