package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.filter.JWTAuthenticationFilter

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
  * registers our required filters and disables certain features for us
  */
class JWTRestSecurityService extends RestSecurityService {

  @Autowired
  val authenticationService: JWTAuthenticationService = null

  /**
    * prepares the http security object for our use
    *
    * @return
    */
  override def prepare(http: HttpSecurity): HttpSecurity = {
    http.csrf().disable().httpBasic().disable().addFilterBefore(new JWTAuthenticationFilter(authenticationService), classOf[UsernamePasswordAuthenticationFilter])
  }
}
