package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.service.RestLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.RestServerConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.config.StatisticsRepositoryConfig
import edu.ucdavis.fiehnlab.mona.backend.services.statistics.server.StatisticServer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Configuration, Import, Primary}

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
  def loginServiceDelegate: LoginService = new PostgresLoginService
}

@Configuration
@Import(Array(classOf[RestServerConfig], classOf[StatisticServer]))
class EmbeddedRestServerConfig extends LazyLogging {

  /**
   * the service which actually does the login for us
   *
   * @return
   */
  @Bean
  def loginService: LoginService = new PostgresLoginService

}
