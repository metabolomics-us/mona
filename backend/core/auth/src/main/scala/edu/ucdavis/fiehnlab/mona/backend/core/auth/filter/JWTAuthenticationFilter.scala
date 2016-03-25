package edu.ucdavis.fiehnlab.mona.backend.core.auth.filter

import java.util
import java.util.Date
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.{FilterChain, ServletException, ServletRequest, ServletResponse}

import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.TokenSecret
import io.jsonwebtoken.{Claims, Jwts, MalformedJwtException}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint
import org.springframework.security.authentication.{AuthenticationManager, AuthenticationServiceException}
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.{Authentication, AuthenticationException, GrantedAuthority}
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.web.filter.GenericFilterBean

import scala.collection.JavaConverters._
/**
  * this filter intercepts all requests and does the authentication for us
  * to ensure our services are protected
  */
class JWTAuthenticationFilter(authenticationManager: AuthenticationManager,tokenSecret:TokenSecret) extends GenericFilterBean {

  val entryPoint: AuthenticationEntryPoint = new Http401AuthenticationEntryPoint("authorization failed!")


  /**
    * TODO doesn't really belong here, should be moved
    * does the authentication for the given token
    *
    * @param token
    * @return
    */
  def authenticate(token:String) : Authentication = {

    val claims:Claims = Jwts.parser().setSigningKey(tokenSecret.value).parseClaimsJws(token).getBody

    new JWTAuthentication(claims)
  }


  /**
    * ensures the user is authenticated and has the correct rights
    *
    * @param servletRequest
    * @param servletResponse
    * @param filterChain
    */
  override def doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain): Unit = {

    logger.debug("filtering...")
    val request = servletRequest.asInstanceOf[HttpServletRequest]
    val response = servletResponse.asInstanceOf[HttpServletResponse]
    logger.debug(s"url: ${request.getRequestURL} with ${request.getMethod}")
    try {
      val authHeader = request.getHeaderNames.asScala.filter( _.toLowerCase() == "authorization").toList

      if(authHeader.isEmpty){
        throw new AuthenticationServiceException("no authorization header provided!")
      }

      val headerValue = request.getHeader(authHeader.head)

      if(!headerValue.trim.toLowerCase.startsWith("bearer")){
        throw new AuthenticationServiceException(s"authorization header was not of type bearer, header was ${authHeader.head}")
      }

      val token = headerValue.trim.substring(7); // The part after "Bearer "

      try {

        val auth = authenticationManager.authenticate(authenticate(token))
        SecurityContextHolder.getContext.setAuthentication(auth)

        logger.debug("continue down the chain...")
        filterChain.doFilter(servletRequest, servletResponse)
      }
      catch {
        case e: AuthenticationException => throw e
        case e: MalformedJwtException => throw new AuthenticationServiceException(s"JWT token was malformed: ${token}",e)
        case e: Exception => throw new AuthenticationServiceException("sorry an unexpected error happened", e)
      }
    }
    catch {
      case e: AuthenticationException =>
        logger.error(e.getMessage,e)
        SecurityContextHolder.clearContext()

        if (entryPoint != null) {
          entryPoint.commence(request, response, e)
        }
    }

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
