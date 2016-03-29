package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.{JWTAuthenticationService, JWTRestSecurityService, JWTTokenService}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
  * provides us with the specifics for a JWT based authentication
  * in the Mona system
  */
@EnableMongoRepositories(basePackageClasses = Array(classOf[UserRepository]))
@ComponentScan(basePackageClasses = Array(classOf[UserRepository]))
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

  /**
    * this configures our filters and services to the http security object
    * @return
    */
  @Bean
  def restSecurityService: JWTRestSecurityService = new JWTRestSecurityService
}
