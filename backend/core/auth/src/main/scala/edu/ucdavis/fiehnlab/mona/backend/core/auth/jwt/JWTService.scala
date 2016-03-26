package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt

import java.util
import java.util.Date

import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.{AuthenticationService, TokenService}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, TokenSecret, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginResponse
import io.jsonwebtoken.{Claims, Jwts, MalformedJwtException, SignatureAlgorithm}
import org.apache.commons.lang.time.DateUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AuthorizationServiceException
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.{Authentication, AuthenticationException, GrantedAuthority}
import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/26/16.
  */
class JWTTokenService extends TokenService {


  @Autowired
  val tokenSecret: TokenSecret = null

  /**
    * time how long a token is valid for in hours
    */
  val timeOfLife = 24

  /**
    * generates a token for us
    * based on the given user
    *
    * @param user
    * @return
    */
  override def generateToken(user: User): String = {

    val issueDate = new Date()
    val experiationDate = DateUtils.addHours(issueDate, timeOfLife)

    //associated roles
    val roles = user.roles.asScala.collect {
      case x: Role => x.name
    }.asJava

   Jwts.builder().setSubject(user.username).claim("roles", roles).setIssuedAt(issueDate).setExpiration(experiationDate).signWith(SignatureAlgorithm.HS256, tokenSecret.value).compact()
  }
}

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
    if(token == null){
      throw new AuthenticationServiceException("sorry no token was provided!")
    }
    val claims: Claims = Jwts.parser().setSigningKey(tokenSecret.value).parseClaimsJws(token).getBody


    try {
      val authentication = new JWTAuthentication(claims)

      //ensure we are up to data

      if(authentication.isExpired){
        throw new AuthorizationServiceException("token was expired!")
      }

      if(authentication.isNotYetActive){
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


/**
  * our custom token based authentication
  *
  * @param claims
  */
final class JWTAuthentication(claims: Claims) extends Authentication {

  var authenticated: Boolean = false

  override def getDetails: AnyRef = claims

  override def getPrincipal: AnyRef = claims.getSubject

  override def isAuthenticated: Boolean = authenticated

  /**
    * generates all roles in the claim
    *
    * @return
    */
  override def getAuthorities: util.Collection[_ <: GrantedAuthority] = claims.get("roles").asInstanceOf[java.util.List[String]].asScala.collect { case x: String => new SimpleGrantedAuthority(x) }.asJava

  override def getCredentials: AnyRef = ""

  override def setAuthenticated(isAuthenticated: Boolean): Unit = authenticated = isAuthenticated

  override def getName: String = claims.getSubject


  /**
    * checks if the provided token is expired or still valid
    * current
    *
    * @return
    */
  def isExpired: Boolean = {
    if (claims.getExpiration != null) {
      claims.getExpiration.before(new Date())
    }
    else {
      false
    }
  }

  /**
    * token is not yet active
    *
    * @return
    */
  def isNotYetActive: Boolean = {
    if (claims.getNotBefore != null) {
      claims.getNotBefore.after(new Date())
    }
    else {
      false
    }
  }
}
