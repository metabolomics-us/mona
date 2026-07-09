package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.deletion.DeletionQueueConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.{EurekaClientConfig, SwaggerConfig}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.config.DownloadConfig
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.config.DownloadListenerConfig
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.upload.UploadQueueConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter, WebSecurityCustomizer}
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.annotation.web.builders.{WebSecurity}

/**
  * Created by sajjan on 5/25/16.
  */
@SpringBootApplication
@EnableWebSecurity
@EnableScheduling
@Order(5)
@Import(Array(classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration],
  classOf[JWTAuthenticationConfig], classOf[SwaggerConfig], classOf[EurekaClientConfig], classOf[DownloadConfig], classOf[DownloadListenerConfig],
  classOf[UploadQueueConfig], classOf[DeletionQueueConfig]))
class DownloadScheduler extends WebSecurityConfigurerAdapter with LazyLogging {

  @Autowired
  val restSecurityService: RestSecurityService = null

  // PostgresLoginService is not annotated for component scanning, every service that resolves
  // callers from tokens defines this bean itself (same as RestPersistenceServer). The upload
  // controller needs it
  @Bean
  def loginService: LoginService = new PostgresLoginService

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
      .antMatchers(HttpMethod.GET, "/rest/downloads/schedule/**").authenticated()

      // must be an admin to schedule re-generation of predefined downloads
      .antMatchers(HttpMethod.GET, "/rest/downloads/generatePredefined").hasAuthority("ADMIN")

      // must be an admin to create predefined queryes
      .antMatchers(HttpMethod.POST, "/rest/downloads/predefined").hasAuthority("ADMIN")

      // must be an admin to reconcile predefined downloads against deleted libraries
      .antMatchers(HttpMethod.POST, "/rest/downloads/predefined/reconcile").hasAuthority("ADMIN")

      // must be an admin to upload static files
      .antMatchers(HttpMethod.POST, "/rest/downloads/static").hasAuthority("ADMIN")

      // uploads are tied to a user, so every upload operation needs a valid token. Per job
      // ownership (a user may only see or delete their own jobs unless admin) is enforced in
      // UploadSchedulerController
      .antMatchers("/rest/uploads/**").authenticated()
      .antMatchers("/rest/uploads").authenticated()
  }

  override def configure(web: WebSecurity): Unit = {
    web.ignoring().antMatchers(HttpMethod.GET, "/*")
      .antMatchers(HttpMethod.GET, "/rest/downloads/retrieve/**")
      .antMatchers(HttpMethod.GET, "/rest/downloads/predefined")
      .antMatchers(HttpMethod.GET, "/rest/downloads/static/**")
  }
}

object DownloadScheduler extends App {
  new SpringApplication(classOf[DownloadScheduler]).run()
}
