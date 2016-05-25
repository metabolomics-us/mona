package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.{ MongoConfig}
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.test.context.TestPropertySource

/**
  * Created by wohlg on 3/25/2016.
  */
@Configuration
@Import(Array(classOf[MongoConfig]))
@TestPropertySource(locations=Array("classpath:application.properties"))
class EmbeddedAuthConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginService: LoginService = new MongoLoginService
}
