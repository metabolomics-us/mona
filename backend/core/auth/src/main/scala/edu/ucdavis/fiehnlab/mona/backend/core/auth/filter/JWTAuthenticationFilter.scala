package edu.ucdavis.fiehnlab.mona.backend.core.auth.filter

import javax.servlet.http.HttpServletRequest
import javax.servlet.{ServletException, FilterChain, ServletResponse, ServletRequest}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.TokenSecret
import io.jsonwebtoken.{SignatureException, Jwts}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.filter.GenericFilterBean

/**
  * a JSON web token based authentification filter
  */
class JWTAuthenticationFilter extends GenericFilterBean{

  /**
    * the secret key used for encoding our keys
    */
  @Autowired
  val tokenSecret:TokenSecret = null

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

    val authHeader = request.getHeader("Authorization")
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      logger.debug(s"header is wrong: ${authHeader}")
      throw new ServletException("Missing or invalid Authorization header.")
    }

    val token = authHeader.substring(7); // The part after "Bearer "

    logger.debug(s"received token ${token}" )

    try {
      val claims = Jwts.parser().setSigningKey(tokenSecret.value)
        .parseClaimsJws(token).getBody()
      request.setAttribute("claims", claims)
    }
    catch{
      case e:SignatureException =>throw new ServletException("Invalid token.")
    }

    logger.debug("continue down the chain...")
    filterChain.doFilter(servletRequest, servletResponse);
  }
}
