package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.EmbeddedElasticSearchConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.context.annotation.{Bean, Configuration, Import}

/**
  * Created by wohlg on 3/11/2016.
  */
@Configuration
@Import(Array(classOf[EmbeddedElasticSearchConfiguration], classOf[MongoConfig], classOf[RestServerConfig]))
class EmbeddedRestServerConfig extends LazyLogging {


  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginService:LoginService = new MongoLoginService

}
@SpringBootApplication(exclude = Array(classOf[MongoAutoConfiguration]))
@Import(Array(classOf[MongoConfig]))
class TestConfig