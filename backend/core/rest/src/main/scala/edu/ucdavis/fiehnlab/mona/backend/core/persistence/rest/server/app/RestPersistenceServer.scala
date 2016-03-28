package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.app

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.RestServerConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.{Bean, Import}

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@EnableDiscoveryClient
@Import(Array(classOf[RestServerConfig],classOf[JWTAuthenticationConfig]))
class RestPersistenceServer {

  //should be done over config serverß
  @Bean
  def tokenSecret: TokenSecret = TokenSecret("sadaskdkljsalkd")

}

object RestPersistenceServer extends App{
  System.setProperty("spring.config.name", "persistence-service");
  new SpringApplication(classOf[RestPersistenceServer]).run()
}
