package edu.ucdavis.fiehnlab.mona.backend.core.auth.service

import java.util
import java.util.Date

import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{LoginRequest, TokenSecret, User}
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

  /**
    * does the authentication for the given token
    *
    * @param token
    * @return
    */
  def authenticate(token:String) : Authentication = {

    val claims:Claims = Jwts.parser().setSigningKey(tokenSecret.value).parseClaimsJws(token).getBody

    new JWTAuthentication(claims)
  }
}

/**
  * our custom token based authentication
  * @param claims
  */
final class JWTAuthentication(claims:Claims) extends Authentication {

  var authenticated:Boolean = false

  override def getDetails: AnyRef = claims

  override def getPrincipal: AnyRef = claims.getSubject

  override def isAuthenticated: Boolean = authenticated

  /**
    * generates all roles in the claim
    *
    * @return
    */
  override def getAuthorities: util.Collection[_ <: GrantedAuthority] = claims.get("roles").asInstanceOf[java.util.List[String]].asScala.collect{case x:String => new SimpleGrantedAuthority(x)}.asJava

  override def getCredentials: AnyRef = ""

  override def setAuthenticated(isAuthenticated: Boolean): Unit = authenticated = isAuthenticated

  override def getName: String = claims.getSubject


  /**
    * checks if the provided token is expired or still valid
    * current
    *
    * @return
    */
  def isExpired : Boolean = {
    if(claims.getExpiration != null) {
      claims.getExpiration.before(new Date())
    }
    else{
      false
    }
  }

  /**
    * token is not yet active
    * @return
    */
  def isNotYetActive : Boolean = {
    if(claims.getNotBefore != null){
      claims.getNotBefore.after(new Date())
    }
    else{
      false
    }
  }
}
