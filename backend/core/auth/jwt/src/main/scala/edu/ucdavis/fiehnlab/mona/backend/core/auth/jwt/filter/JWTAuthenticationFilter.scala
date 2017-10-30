package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.filter

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.{FilterChain, ServletRequest, ServletResponse}

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.JWTAuthenticationService
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.web.filter.GenericFilterBean

import scala.collection.JavaConverters._
/**
  * this filter will ensure JWT based authentication is working as supposed
  */
class JWTAuthenticationFilter(authenticationService:JWTAuthenticationService) extends GenericFilterBean {

  val entryPoint: AuthenticationEntryPoint = new Http401AuthenticationEntryPoint("authorization failed!")

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
        throw new AuthenticationServiceException(s"No authorization header provided! Request was ${request.getRequestURI} and method was ${request.getMethod}")
      }

      val headerValue = request.getHeader(authHeader.head)

      logger.info(s"received header: ${headerValue}")
      if(!headerValue.trim.toLowerCase.startsWith("bearer")){
        throw new AuthenticationServiceException(s"Authorization header was not of type bearer, header was ${authHeader.head}")
      }


      try {
        val token = headerValue.trim.substring(7); // The part after "Bearer "

        assert(token != null)
        val auth = authenticationService.authenticate(token)
        SecurityContextHolder.getContext.setAuthentication(auth)

        logger.debug("continue down the chain...")
        filterChain.doFilter(servletRequest, servletResponse)
      } catch {
        case e: AuthenticationException =>
          logger.error(e.getMessage,e)
          throw e
        case e: Exception =>
          logger.error(e.getMessage,e)
          throw new AuthenticationServiceException(s"Sorry an unexpected error occurred: ${e.getMessage}", e)
      }
    } catch {
      case e: AuthenticationException =>
        logger.error(e.getMessage)
        logger.debug(e)
        SecurityContextHolder.clearContext()

        if (entryPoint != null) {
          entryPoint.commence(request, response, e)
        }
    }
  }
}
