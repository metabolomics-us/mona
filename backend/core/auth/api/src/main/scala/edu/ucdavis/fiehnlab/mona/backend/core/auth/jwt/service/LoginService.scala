package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service

import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginRequest, LoginResponse}
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.Authentication


/**
  * a simple service to provide us with a authentication based
  * on a token ß
  */
trait AuthenticationService {

  def authenticate(token:String) : Authentication


}

/**
  * a simple service to provide us with tokens for usersß
  */
trait TokenService {

  /**
    * generates a token for us
    * based on the given user
    * @param user
    * @return
    */
  def generateToken(user:User) : String
}

/**
  * a simple helper interface to allow easier configuration of the security context of rest servicesß
  */
trait RestSecurityService {

  /**
    * prepares the http security object for our use
    * @param httpSecurity
    * @return
    */
  def prepare(httpSecurity: HttpSecurity) : HttpSecurity
}