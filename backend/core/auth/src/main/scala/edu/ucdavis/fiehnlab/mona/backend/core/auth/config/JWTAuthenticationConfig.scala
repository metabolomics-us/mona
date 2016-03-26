package edu.ucdavis.fiehnlab.mona.backend.core.auth.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.{JWTAuthenticationService, JWTTokenService}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.{AuthenticationService, TokenService}
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * provides us with the specifics for a JWT based authentication
  * in the Mona system
  */
@Configuration
class JWTAuthenticationConfig {

  /**
    * this services defines how we would like to have our tokens generated
    * @return
    */
  @Bean
  def tokenService:JWTTokenService = new JWTTokenService

  /**
    * the service defines how we authenticate the requests
    * @return
    */
  @Bean
  def authenticationService:JWTAuthenticationService = new JWTAuthenticationService

}
