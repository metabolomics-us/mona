package edu.ucdavis.fiehnlab.mona.backend.core.auth.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.{LoginService, MongoLoginService}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.TokenSecret
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * Created by wohlg on 3/25/2016.
  */
@Configuration
class EmbeddedAuthConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginService:LoginService = new MongoLoginService


  /**
    * the token secret used during the testing phase
    *
    * @return
    */
  @Bean
  def tokenSecret: TokenSecret = TokenSecret("sadaskdkljsalkd")
}
