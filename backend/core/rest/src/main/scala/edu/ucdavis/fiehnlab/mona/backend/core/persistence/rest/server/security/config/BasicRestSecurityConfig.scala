package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.security.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.{WebSecurityConfigurerAdapter, EnableWebSecurity}
import org.springframework.security.config.http.SessionCreationPolicy

/**
  * utilizes a httpBasic authentification for security resons
  */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity
class BasicRestSecurityConfig extends WebSecurityConfigurerAdapter {

  /**
    * does our basic authentification for us
    *
    * @param http
    */
  override def configure(http: HttpSecurity): Unit = {
    http
      .csrf().disable()
      .httpBasic()
      .and()
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
}


