package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.app

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.RestServerConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.{EurekaClientConfig, SwaggerConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.config.StatisticsRepositoryConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Import, Profile}
import org.springframework.scheduling.annotation.EnableScheduling

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication(scanBasePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql"))
@Import(Array(classOf[RestServerConfig], classOf[JWTAuthenticationConfig], classOf[SwaggerConfig], classOf[EurekaClientConfig], classOf[StatisticsRepositoryConfig], classOf[PostgresqlConfiguration]))
@EnableScheduling
class RestPersistenceServer {

  @Bean
  def loginService: LoginService = new PostgresLoginService
}

object RestPersistenceServer extends App {
  System.setProperty("spring.config.name", "persistence-service")
  val app = new SpringApplication(classOf[RestPersistenceServer])
  app.setAdditionalProfiles("mona.persistence.init")
  app.setAdditionalProfiles("mona.persistence")
  app.run()
}
