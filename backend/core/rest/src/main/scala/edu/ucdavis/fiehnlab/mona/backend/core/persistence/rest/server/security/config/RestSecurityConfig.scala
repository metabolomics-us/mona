package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.security.config

import java.util

import edu.ucdavis.fiehnlab.mona.backend.core.auth.config.AuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.filter.JWTAuthenticationFilter
import edu.ucdavis.fiehnlab.mona.backend.core.auth.provider.{JWTAuthenticationProvider, MonaAuthenticationProvider}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.LoginService
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
  * basic security class for our usage
  */
abstract class RestSecurity extends WebSecurityConfigurerAdapter {

  /**
    * does our basic authentification for us
    *
    * @param http
    */
  override final def configure(http: HttpSecurity): Unit = {
    prepare(http)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()
      //saves need to be authentifiated
      .antMatchers(HttpMethod.POST, "/rest/spectra/**").authenticated()
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
    * here we configure what needs no security
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
  * utilizes basic authentication
  */

/**
  * utilizes a httpBasic authentication for security resons
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

  @Bean
  def monaAuthenticationProvider: MonaAuthenticationProvider = new MonaAuthenticationProvider

}

/**
  * utilizes JWT based authentication
  */
@Import(Array(classOf[AuthenticationConfig]))
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity
class JWTRestSecurityConfig extends RestSecurity {

  /**
    * the actual manager
    * @param provider
    * @return
    */
  @Bean
  def authenticationManager(provider:AuthenticationProvider): AuthenticationManager = new ProviderManager(List[AuthenticationProvider](provider).asJava)

  @Bean
  def filter:JWTAuthenticationFilter = new JWTAuthenticationFilter

  /**
    * token based authentication
    *
    * @return
    */
  @Bean
  def authenticationProvider: JWTAuthenticationProvider = new JWTAuthenticationProvider()

  /**
    * prepares our security object
    *
    * @param http
    * @return
    */
  override def prepare(http: HttpSecurity): HttpSecurity = {
    http.csrf().disable().httpBasic().disable().addFilterBefore(filter,classOf[UsernamePasswordAuthenticationFilter])
  }
}