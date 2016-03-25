package edu.ucdavis.fiehnlab.mona.backend.core.auth.provider

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.{JWTAuthentication, LoginService}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AuthorizationServiceException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication


/**
  * token based authentication provider
  */
class JWTAuthenticationProvider extends AuthenticationProvider with LazyLogging{

  @Autowired
  val loginService:LoginService = null

  override def authenticate(authentication: Authentication): Authentication = {

    logger.debug("attempting authentication...")
    val auth = authentication.asInstanceOf[JWTAuthentication]

    if(auth.isExpired){
      throw new AuthorizationServiceException("token was expired!")
    }

    if(auth.isNotYetActive){
      throw new AuthorizationServiceException("token is not yet valid!")
    }
    auth.setAuthenticated(true)

    logger.debug("=> success")
    auth
  }

  /**
    * it has to be an authentication of type JWT
    *
    * @param authentication
    * @return
    */
  override def supports(authentication: Class[_]): Boolean = classOf[JWTAuthentication].isAssignableFrom(authentication)
}