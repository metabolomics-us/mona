package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Configuration, Import}

/**
  * Created by wohlg on 3/11/2016.
  */
@Configuration
@Import(Array(classOf[RestServerConfig]))
class EmbeddedRestServerConfig extends LazyLogging {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginService: LoginService = new PostgresLoginService

}

@SpringBootApplication(scanBasePackageClasses = Array())
@Import(Array(classOf[PostgresqlConfiguration]))
class TestConfig
