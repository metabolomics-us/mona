package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.config.WebHookSecurity
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Import}

/**
  * Created by wohlg on 4/11/2016.
  */
@SpringBootApplication
@Import(Array(classOf[MongoConfig],classOf[WebHookSecurity], classOf[JWTAuthenticationConfig]))
class TestConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginServiceDelegate: LoginService = new MongoLoginService


  /**
    * the token secret used during the testing phase
    *
    * @return
    */
  @Bean
  def tokenSecret: TokenSecret = TokenSecret("sadaskdkljsalkd")

}
