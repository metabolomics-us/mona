package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.service.RestLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.EmbeddedRestServerConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.config.StatisticsRepositoryConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Import, Primary}

/**
  * Created by wohlgemuth on 3/15/16.
  */
@Import(Array(classOf[RestClientConfig], classOf[EmbeddedRestServerConfig], classOf[AuthSecurityConfig], classOf[StatisticsRepositoryConfig]))
@SpringBootApplication
class RestClientTestConfig {

  @Bean
  @Primary
  def loginService(@Value("${mona.rest.server.host}") host: String, @Value("${mona.rest.server.port}") port: Int): LoginService =
    new RestLoginService("localhost", port)

  @Bean
  def loginServiceDelegate: LoginService = new MongoLoginService
}