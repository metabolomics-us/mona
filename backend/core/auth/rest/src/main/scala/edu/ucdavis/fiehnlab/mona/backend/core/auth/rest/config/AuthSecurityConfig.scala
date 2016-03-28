package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller.LoginController
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import org.springframework.context.annotation.{ComponentScan, Configuration}
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
  * Created by wohlg on 3/27/2016.
  */
@Order(1)
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[LoginController],classOf[DomainConfig]))
class AuthSecurityConfig extends WebSecurityConfigurerAdapter {

  /**
    * ignore post requests to the auth url
    *
    * @param web
    */
  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      .antMatchers(HttpMethod.POST, "/rest/auth/**")

  }

  override def configure(http: HttpSecurity): Unit = http.csrf().disable()
}
