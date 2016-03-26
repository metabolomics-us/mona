package edu.ucdavis.fiehnlab.mona.backend.core.auth.service

import java.util
import java.util.Date

import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{TokenSecret, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginRequest
import io.jsonwebtoken.{Claims, Jwts}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.{Authentication, GrantedAuthority}

import scala.collection.JavaConverters._

/**
  * a login and token validation service
  */
trait LoginService {

  @Autowired
  val tokenSecret:TokenSecret = null

  /**
    * does a login for the current user
    *
    * @return
    */
  def login(request: LoginRequest) : User

}
