package edu.ucdavis.fiehnlab.mona.backend.core.auth.provider

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.Role
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.{AuthenticationProvider, UsernamePasswordAuthenticationToken}
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

import scala.collection.JavaConverters._

/**
  * implementation of an authentication provider in case people
  * want to use the direct database approach. Mostly interessting in case of Basic authentication
  */
class MonaAuthenticationProvider extends AuthenticationProvider with LazyLogging {

  /**
    * a service todo the actual logins for us
    */
  @Autowired
  val loginService: LoginService = null

  /**
    * provides us with authentication against the mona submitter repository
    *
    * @param authentication
    * @return
    */
  override def authenticate(authentication: Authentication): Authentication = {

    val name = authentication.getName
    val password = authentication.getCredentials.toString

    val user = loginService.login(LoginRequest(name, password))


    val roles = user.roles.asScala.collect {
      case x: Role => new SimpleGrantedAuthority(x.name)
    }.toList.asJava

    new UsernamePasswordAuthenticationToken(name, password, roles)

  }

  /**
    * which classes we support for authentication
    *
    * @param authentication
    * @return
    */
  override def supports(authentication: Class[_]): Boolean = {
    authentication.equals(classOf[UsernamePasswordAuthenticationToken])

  }
}
