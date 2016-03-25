package edu.ucdavis.fiehnlab.mona.backend.core.auth.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.controller.LoginController
import edu.ucdavis.fiehnlab.mona.backend.core.auth.filter.JWTAuthenticationFilter
import edu.ucdavis.fiehnlab.mona.backend.core.auth.provider.MonaAuthenticationProvider
import edu.ucdavis.fiehnlab.mona.backend.core.auth.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.{MongoLoginService, LoginService}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import org.springframework.context.annotation._
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories


/**
  * Created by wohlgemuth on 3/24/16.
  */
@Configuration
@Import(Array(classOf[MongoConfig]))
@EnableMongoRepositories(basePackageClasses = Array(classOf[UserRepository]))
@ComponentScan(basePackageClasses = Array(classOf[UserRepository],classOf[LoginController]))
class AuthenticationConfig {

  /**
    * provides us with access to the required authentication provider
    * to be used by our applications
 *
    * @return
    */
  @Bean
  def authenticationProvider:MonaAuthenticationProvider = new MonaAuthenticationProvider()

  /**
    * the service which actually does the logins for us
    *
    * @return
    */
  @Bean
  def loginService:LoginService = new MongoLoginService

  /**
    * secret used for encryption of the token
    *
    * @return
    */
  @Bean
  def tokenSecret: TokenSecret = TokenSecret("sadaskdkljsalkd")
}
