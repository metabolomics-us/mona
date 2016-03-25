package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.security.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.config.AuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.filter.JWTAuthenticationFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Import, Configuration}
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.{WebSecurityConfigurerAdapter, EnableWebSecurity}
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

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
      .antMatchers(HttpMethod.POST, "/rest/metaData/**").permitAll()
      //updates needs authentication
      .antMatchers(HttpMethod.PUT).authenticated()
      //deletes need authentication
      .antMatchers(HttpMethod.DELETE).hasAuthority("ADMIN")

      //anything goes
      .anyRequest().permitAll()

  }

  /**
    * prepares our security object
    *
    * @param http
    * @return
    */
  def prepare(http: HttpSecurity): HttpSecurity
}


/**
  * utilizes basic authentication
  */

/**
  * utilizes a httpBasic authentification for security resons
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
    * our custom filter for token authentication
    *
    * @return
    */
  @Bean
  def jwtFilter: JWTAuthenticationFilter = new JWTAuthenticationFilter

  /**
    * prepares our security object
    *
    * @param http
    * @return
    */
  override def prepare(http: HttpSecurity): HttpSecurity = {
    http.httpBasic().disable().addFilterBefore(jwtFilter, classOf[UsernamePasswordAuthenticationFilter])
  }
}