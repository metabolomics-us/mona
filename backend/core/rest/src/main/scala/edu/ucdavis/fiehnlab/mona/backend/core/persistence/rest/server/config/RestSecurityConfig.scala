package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.config.{AuthenticationConfig, JWTAuthenticationConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.{JWTAuthenticationFilter, JWTAuthenticationService, JWTTokenService}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.TokenSecret
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.{AuthenticationManager, AuthenticationProvider, ProviderManager}
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


/**
  * this is an abstract base class and utilized to secure the different routes in Mona
  * it should be implemented by concreate classes, which provide the actual security configuration
  *
  * please be aware that only 1 class should be used for authorization, since they are mutably exclusive
  *
  */
abstract class RestSecurity extends WebSecurityConfigurerAdapter {

  /**
    * this method configures authorized access to the system
    * and protects the urls with the specified methods and credentials
    * @param http
    */
  override final def configure(http: HttpSecurity): Unit = {
    prepare(http)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()
      //saves need to be authentifiated
      .antMatchers(HttpMethod.POST, "/rest/spectra/**").authenticated()
      .antMatchers(HttpMethod.POST, "/rest/submitters/**").authenticated()

      //updates needs authentication
      .antMatchers(HttpMethod.PUT).authenticated()
      //deletes need authentication
      .antMatchers(HttpMethod.DELETE).hasAuthority("ADMIN")
      //nothing goes
      .anyRequest().denyAll()

  }

  /**
    * prepares our security object
    *
    * @param http
    * @return
    */
  def prepare(http: HttpSecurity): HttpSecurity

  /**
    * this method configures, which parts of the system and which methods do not need
    * any form of security in place and can be openly accessed
    *
    * @param web
    */
  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      //get is always available
      .antMatchers(HttpMethod.GET)

      //no authentication for metadata
      .antMatchers(HttpMethod.POST, "/rest/metaData/**")

  }
}

/**
  * this configuration provides a Json Web Token based configuration
  * and requires the user to first login
  * and than submit his token with every request
  *
  * this has to be done utilizing the Authorization header and the Bearer schema
  */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity
@Import(Array(classOf[JWTAuthenticationConfig]))
class JWTRestSecurityConfig extends RestSecurity {

  @Autowired
  val authenticationService:JWTAuthenticationService = null

  /**
    * prepares our security object
    *
    * @param http
    * @return
    */
  override def prepare(http: HttpSecurity): HttpSecurity = {
    http.csrf().disable().httpBasic().disable().addFilterBefore(new JWTAuthenticationFilter(authenticationService),classOf[UsernamePasswordAuthenticationFilter])
  }
}