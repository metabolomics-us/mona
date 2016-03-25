package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.security.config

import java.util

import edu.ucdavis.fiehnlab.mona.backend.core.auth.config.AuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.filter.JWTAuthenticationFilter
import edu.ucdavis.fiehnlab.mona.backend.core.auth.provider.{JWTAuthenticationProvider, MonaAuthenticationProvider}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.TokenSecret
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.{AuthenticationManager, AuthenticationProvider, ProviderManager}
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import scala.collection.JavaConverters._

/**
  * this is an abstract base class and utilized to secure the different routes in Mona
  * it should be implemented by concreate classes, which provide the actual security configuration
  *
  * please be aware that only 1 class should be used for authorization, since they are mutably exclusive
  *
  */
abstract class RestSecurity extends WebSecurityConfigurerAdapter {

  /**
    * this bean with configure our custom authentication providers into the central authentication manager
    * which will be used in the security configurations
    */
  @Bean
  def authenticationManager(provider:AuthenticationProvider): AuthenticationManager = new ProviderManager(List[AuthenticationProvider](provider).asJava)

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

      //no authentication for authentication
      .antMatchers(HttpMethod.POST, "/rest/auth/login")
  }
}

/**
  * this configuration provides you with http basic authentication and
  * utilizes the mona authentication provider internally
  */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity
class BasicRestSecurityConfig extends RestSecurity {
  /**
    * prepares our security object
    *
    * @param http
    * @return
    */
  override def prepare(http: HttpSecurity): HttpSecurity = http.csrf().disable().httpBasic().and()

  /**
    * executes the authentication for us
    * @return
    */
  @Bean
  def authenticationProvider: MonaAuthenticationProvider = new MonaAuthenticationProvider

}

/**
  * this configuration provides a Json Web Token based configuration
  * and requires the user to first login
  * and than submit his token with every request
  *
  * this has to be done utilizing the Authorization header and the Bearer schema
  */
@Import(Array(classOf[AuthenticationConfig]))
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity
class JWTRestSecurityConfig extends RestSecurity {

  /**
    * token based authentication provider
    *
    * @return
    */
  @Bean
  def authenticationProvider: JWTAuthenticationProvider = new JWTAuthenticationProvider()

  @Autowired
  val token:TokenSecret = null
  /**
    * prepares our security object
    *
    * @param http
    * @return
    */
  override def prepare(http: HttpSecurity): HttpSecurity = {
    http.csrf().disable().httpBasic().disable().addFilterBefore(new JWTAuthenticationFilter(authenticationManager(),token),classOf[UsernamePasswordAuthenticationFilter])
  }
}