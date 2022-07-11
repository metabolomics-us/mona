package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}
import org.springframework.test.context.TestPropertySource

/**
  * Created by wohlg on 3/25/2016.
  */
@SpringBootApplication(scanBasePackageClasses = Array())
@Import(Array(classOf[PostgresqlConfiguration]))
class EmbeddedAuthConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginService: LoginService = new PostgresLoginService
}
