package edu.ucdavis.fiehnlab.mona.backend.services.downloader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.SwaggerConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.security.config.http.SessionCreationPolicy
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
  * Created by sajjan on 5/25/16.
  */
@SpringBootApplication
@EnableDiscoveryClient
@EnableWebSecurity
@EnableSwagger2
@Order(5)
@Import(Array(classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration], classOf[MongoConfig],
  classOf[JWTAuthenticationConfig], classOf[SwaggerConfig]))
class DownloadScheduler extends WebSecurityConfigurerAdapter with LazyLogging {

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
      .antMatchers(HttpMethod.GET, "/rest/downloads/schedule/*").hasAuthority("USER")

      // must be an admin to schedule re-generation of predefined downloads
      .antMatchers(HttpMethod.GET, "/rest/downloads/schedulePredefinedDownloads").hasAuthority("ADMIN")
  }

  override def configure(web: WebSecurity): Unit = {
    web.ignoring().antMatchers(HttpMethod.GET, "/*")
  }
}

object DownloadScheduler extends App {
  new SpringApplication(classOf[DownloadScheduler]).run()
}
