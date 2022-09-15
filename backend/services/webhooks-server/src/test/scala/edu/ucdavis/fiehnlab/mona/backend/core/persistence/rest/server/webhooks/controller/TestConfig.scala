package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.MonaNotificationBusCounterConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.app.WebHookServer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Import, Primary}

/**
  * Created by wohlg on 4/11/2016.
  */
@SpringBootApplication(scanBasePackages = Array())
@Import(Array(classOf[JWTAuthenticationConfig], classOf[WebHookServer], classOf[MonaNotificationBusCounterConfiguration]))
class TestConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  @Primary
  def loginServiceDelegate: LoginService = new PostgresLoginService


  /**
    * the token secret used during the testing phase
    *
    * @return
    */
  @Bean
  def tokenSecret: TokenSecret = TokenSecret("sadaskdkljsalkd")
}
