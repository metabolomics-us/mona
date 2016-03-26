package edu.ucdavis.fiehnlab.mona.backend.core.auth.service

import java.util
import java.util.Date

import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{TokenSecret, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginRequest, LoginResponse}
import io.jsonwebtoken.{Claims, Jwts}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.{Authentication, GrantedAuthority}

import scala.collection.JavaConverters._

/**
  * a login and token validation service
  */
trait LoginService {

  /**
    * does a login for the current user
    * and returns a response or throws a related exception
    *
    * @return
    */
  def login(request: LoginRequest) : LoginResponse

}


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