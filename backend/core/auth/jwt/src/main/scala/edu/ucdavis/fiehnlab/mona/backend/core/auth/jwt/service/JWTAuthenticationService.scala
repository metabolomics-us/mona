package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service

import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.AuthenticationService
import io.jsonwebtoken.{Claims, Jwts, MalformedJwtException}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AuthorizationServiceException
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication

/**
  * provides us with a token based authorization service
  */
class JWTAuthenticationService extends AuthenticationService {

  @Autowired
  val tokenSecret: TokenSecret = null

  /**
    * parses the claim informations
    * and ensures that the token is valid for use
    *
    * @param token
    * @return
    */
  override def authenticate(token: String): Authentication = {
    if (token == null) {
      throw new AuthenticationServiceException("sorry no token was provided!")
    }
    val claims: Claims = Jwts.parser().setSigningKey(tokenSecret.value).parseClaimsJws(token).getBody


    try {
      val authentication = new JWTAuthentication(claims)

      //ensure we are up to data

      if (authentication.isExpired) {
        throw new AuthorizationServiceException("token was expired!")
      }

      if (authentication.isNotYetActive) {
        throw new AuthorizationServiceException("token is not yet valid!")
      }
      authentication.setAuthenticated(true)
      authentication
    }
    catch {
      case e: MalformedJwtException => throw new AuthenticationServiceException(s"JWT token was malformed: ${token}", e)
    }
  }
}
