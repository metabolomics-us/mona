package edu.ucdavis.fiehnlab.mona.backend.core.auth.filter

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.{FilterChain, ServletException, ServletRequest, ServletResponse}

import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.LoginService
import io.jsonwebtoken.MalformedJwtException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint
import org.springframework.security.authentication.{AuthenticationManager, AuthenticationServiceException}
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.web.filter.GenericFilterBean

import scala.collection.JavaConverters._
/**
  * this filter intercepts all requests and does the authentication for us
  * to ensure our services are protected
  */
class JWTAuthenticationFilter extends GenericFilterBean {

  @Autowired
  val authenticationManager: AuthenticationManager = null

  @Autowired(required = false)
  val entryPoint: AuthenticationEntryPoint = new Http401AuthenticationEntryPoint("authorization failed!")

  /**
    * the secret key used for encoding our keys
    */
  @Autowired
  val loginService: LoginService = null

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

        val auth = authenticationManager.authenticate(loginService.authenticate(token))
        SecurityContextHolder.getContext.setAuthentication(auth)

        logger.debug("continue down the chain...")
        filterChain.doFilter(servletRequest, servletResponse)
      }
      catch {
        case e: AuthenticationException => throw e
        case e: MalformedJwtException => throw new AuthenticationServiceException(s"JWT token was malformed: ${token}",e)
        case e: Exception => throw new AuthenticationServiceException("sorry an unexspected error happened", e)
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
