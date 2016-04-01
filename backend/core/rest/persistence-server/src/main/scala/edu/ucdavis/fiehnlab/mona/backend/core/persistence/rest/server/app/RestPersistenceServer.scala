package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.app

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.RestServerConfig
import edu.ucdavis.fiehnlab.mona.backend.core.service.listener.AkkaEventScheduler
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.{Bean, Import}

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@EnableDiscoveryClient
@Import(Array(classOf[RestServerConfig],classOf[JWTAuthenticationConfig]))
class RestPersistenceServer {

  @Value("${mona.security.secret}")
  val secret:String = null

  @Bean
  def tokenSecret: TokenSecret = TokenSecret(secret)


  /**
    * we should always use an async event scheduler for the processing of data, if possible
    * @return
    */
  @Bean
  def eventScheduler:AkkaEventScheduler[Spectrum] = {
    new AkkaEventScheduler[Spectrum]
  }
}

object RestPersistenceServer extends App{
  System.setProperty("spring.config.name", "persistence-service");
  new SpringApplication(classOf[RestPersistenceServer]).run()
}
