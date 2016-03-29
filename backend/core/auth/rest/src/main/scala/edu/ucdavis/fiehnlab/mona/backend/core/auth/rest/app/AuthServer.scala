package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.app

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.servcie.LoginService
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.{Bean, Import}

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@EnableDiscoveryClient
@Import(Array(classOf[AuthSecurityConfig],classOf[JWTAuthenticationConfig]))
class AuthServer {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginServiceDelegate:LoginService = new MongoLoginService


  /**
    * need a central server for getting this value otherwise all hell breaks loos
    *
    * @return
    */
  @Bean
  def tokenSecret: TokenSecret = TokenSecret("sadaskdkljsalkd")

}

object AuthServer extends App{
  System.setProperty("spring.config.name", "auth-service");
  new SpringApplication(classOf[AuthServer]).run()
}