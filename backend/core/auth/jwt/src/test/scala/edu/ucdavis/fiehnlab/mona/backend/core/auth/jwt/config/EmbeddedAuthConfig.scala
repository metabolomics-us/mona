package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.{ MongoConfig}
import org.springframework.context.annotation.{Bean, Configuration, Import}

/**
  * Created by wohlg on 3/25/2016.
  */
@Configuration
@Import(Array(classOf[MongoConfig]))
class EmbeddedAuthConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginService:LoginService = new MongoLoginService

}
