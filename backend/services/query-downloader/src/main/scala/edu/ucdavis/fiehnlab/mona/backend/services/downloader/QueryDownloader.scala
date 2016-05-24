package edu.ucdavis.fiehnlab.mona.backend.services.downloader

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.MonaEventBusConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.{WebSecurity, HttpSecurity}
import org.springframework.security.config.annotation.web.configuration.{WebSecurityConfigurerAdapter, EnableWebSecurity}
import org.springframework.security.config.http.SessionCreationPolicy


/**
  * Created by sajjan on 5/16/16.
  */
@SpringBootApplication
@EnableWebSecurity
@Import(Array(classOf[MonaEventBusConfiguration],classOf[JWTAuthenticationConfig]))
class QueryDownloader extends WebSecurityConfigurerAdapter {
  @Autowired
  val restSecurityService:RestSecurityService = null

  /**
    * only authenticated users can schedule downloads from the system
    *
    * @param http
    */
  override final def configure(http: HttpSecurity): Unit = {
    restSecurityService.prepare(http)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()

      // users need to be authenticated to schedule downloads
      .antMatchers(HttpMethod.GET, "/rest/download/schedule/*").hasAuthority("USER")
  }

  override def configure(web: WebSecurity): Unit = {
    web.ignoring().antMatchers(HttpMethod.GET, "/*")
  }
}

/**
  * Created by sajjan on 5/19/16.
  */
object QueryDownloader extends App {
  new SpringApplication(classOf[QueryDownloader]).run()
}
