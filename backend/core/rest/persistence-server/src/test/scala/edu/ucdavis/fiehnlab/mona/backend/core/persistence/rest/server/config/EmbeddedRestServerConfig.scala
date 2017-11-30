package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.config.StatisticsRepositoryConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
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
  def loginService: LoginService = new MongoLoginService

}

@SpringBootApplication(exclude = Array(classOf[MongoAutoConfiguration]))
@Import(Array(classOf[MongoConfig], classOf[StatisticsRepositoryConfig]))
class TestConfig