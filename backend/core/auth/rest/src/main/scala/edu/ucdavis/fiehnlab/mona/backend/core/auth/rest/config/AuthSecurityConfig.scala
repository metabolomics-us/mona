package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller.LoginController
import org.springframework.context.annotation.{ComponentScan, Configuration}
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
  * Created by wohlg on 3/27/2016.
  */
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[LoginController]))
class AuthSecurityConfig extends WebSecurityConfigurerAdapter {

  /**
    * ignore post requests to the auth url
    * @param web
    */
  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      .antMatchers(HttpMethod.POST, "/rest/auth/**")

  }
}
