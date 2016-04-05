package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller.LoginController
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{ComponentScan, Configuration}
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

/**
  * Created by wohlg on 3/27/2016.
  */
@Order(1)
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[LoginController],classOf[DomainConfig]))
class AuthSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  val restSecurityService:RestSecurityService = null

  /**
    * this method configures authorized access to the system
    * and protects the urls with the specified methods and credentials
    * @param http
    */
  override final def configure(http: HttpSecurity): Unit = {
    restSecurityService.prepare(http)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()
      //saves need to be authenticated
      .antMatchers(HttpMethod.POST, "/rest/users/**").authenticated()

      //updates needs authentication
      .antMatchers(HttpMethod.PUT).authenticated()
      //deletes need authentication
      .antMatchers(HttpMethod.DELETE).hasAuthority("ADMIN")
  }
  /**
    * ignore post requests to the auth url
    *
    * @param web
    */
  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      .antMatchers(HttpMethod.POST, "/rest/auth/**")
      .antMatchers(HttpMethod.GET, "/rest/auth/info")
      .antMatchers(HttpMethod.GET, "/rest/users/**")

  }

}
