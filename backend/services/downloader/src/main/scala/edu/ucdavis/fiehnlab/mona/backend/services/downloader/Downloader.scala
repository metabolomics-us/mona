package edu.ucdavis.fiehnlab.mona.backend.services.downloader

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.{WebSecurityConfigurerAdapter, EnableWebSecurity}
import org.springframework.security.config.http.SessionCreationPolicy

/**
  * Created by sajjan on 5/25/16.
  */
@SpringBootApplication
@EnableDiscoveryClient
@EnableWebSecurity
@Order(5)
@Import(Array(classOf[MongoConfig], classOf[JWTAuthenticationConfig]))
class Downloader extends WebSecurityConfigurerAdapter {

  @Autowired
  val restSecurityService: RestSecurityService = null

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
      .antMatchers(HttpMethod.GET, "/rest/downloads/schedule/*").hasAuthority("ADMIN")
  }

  override def configure(web: WebSecurity): Unit = {
    web.ignoring().antMatchers(HttpMethod.GET, "/*")
  }
}


object Downloader extends App {
  new SpringApplication(classOf[Downloader]).run()
}
