package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.app

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.RestServerConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.{EurekaClientConfig, SwaggerConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.config.StatisticsRepositoryConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.scheduling.annotation.EnableScheduling

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@Import(Array(classOf[RestServerConfig], classOf[JWTAuthenticationConfig], classOf[SwaggerConfig], classOf[EurekaClientConfig], classOf[StatisticsRepositoryConfig]))
@EnableScheduling
class RestPersistenceServer {

  @Bean
  def loginService: LoginService = new MongoLoginService
}

object RestPersistenceServer extends App {
  System.setProperty("spring.config.name", "persistence-service")
  new SpringApplication(classOf[RestPersistenceServer]).run()
}
