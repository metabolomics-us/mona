package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.filter

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.{FilterChain, ServletRequest, ServletResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.JWTAuthenticationService
import org.springframework.http.HttpStatus
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.web.filter.GenericFilterBean

import scala.jdk.CollectionConverters._

/**
  * this filter will ensure JWT based authentication is working as supposed
  */
class JWTAuthenticationFilter(authenticationService: JWTAuthenticationService) extends GenericFilterBean {

  val entryPoint: AuthenticationEntryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)

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

    // Only token validation runs inside the auth try/catch. The exception conversion here turns
    // failures into a 401. The filter chain itself is invoked AFTER this block so that downstream
    // application exceptions propagate as real 4xx/5xx instead of being masked as authentication errors
    val authenticated: Boolean =
      try {
        val authHeader = request.getHeaderNames.asScala.filter(_.toLowerCase() == "authorization").toList

        if (authHeader.isEmpty) {
          throw new AuthenticationServiceException(s"No authorization header provided! Request was ${request.getRequestURI} and method was ${request.getMethod}")
        }

        val headerValue = request.getHeader(authHeader.head)

        logger.debug(s"received header: $headerValue")

        if (!headerValue.trim.toLowerCase.startsWith("bearer")) {
          throw new AuthenticationServiceException(s"Authorization header was not of type bearer, header was ${authHeader.head}")
        }

        val token = headerValue.trim.substring(7); // The part after "Bearer "
        assert(token != null)

        val auth =
          try {
            authenticationService.authenticate(token)
          } catch {
            case e: AuthenticationException => throw e
            case e: Exception =>
              throw new AuthenticationServiceException(s"Token validation failed: ${e.getMessage}", e)
          }

        SecurityContextHolder.getContext.setAuthentication(auth)
        true
      } catch {
        case e: AuthenticationException =>
          logger.error(e.getMessage)
          logger.debug(e)
          SecurityContextHolder.clearContext()

          if (entryPoint != null) {
            entryPoint.commence(request, response, e)
          }
          false
      }

    if (authenticated) {
      logger.debug("continue down the chain...")
      filterChain.doFilter(servletRequest, servletResponse)
    }
  }
}
