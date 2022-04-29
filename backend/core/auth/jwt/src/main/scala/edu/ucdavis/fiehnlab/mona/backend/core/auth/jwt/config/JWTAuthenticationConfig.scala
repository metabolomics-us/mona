package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.{JWTAuthenticationService, JWTRestSecurityService, JWTTokenService}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
  * provides us with the specifics for a JWT based authentication
  * in the Mona system
  */
@EnableMongoRepositories(basePackageClasses = Array(classOf[UserRepository]))
@EnableJpaRepositories
@ComponentScan(basePackageClasses = Array(classOf[UserRepository]))
@Configuration
@EnableAutoConfiguration
class JWTAuthenticationConfig {

  @Value("${mona.security.secret:234234324234ewrdsfdsgfsdfw4er32}")
  val secret: String = null

  /**
    * need a central server for getting this value otherwise all hell breaks loos
    *
    * @return
    */
  @Bean
  def tokenSecret: TokenSecret = TokenSecret(secret)

  /**
    * this services defines how we would like to have our tokens generated
    *
    * @return
    */
  @Bean
  def tokenService: JWTTokenService = new JWTTokenService

  /**
    * the service defines how we authenticate the requests
    *
    * @return
    */
  @Bean
  def authenticationService: JWTAuthenticationService = new JWTAuthenticationService

  /**
    * this configures our filters and services to the http security object
    *
    * @return
    */
  @Bean
  def restSecurityService: JWTRestSecurityService = new JWTRestSecurityService
}
