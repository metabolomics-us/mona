package edu.ucdavis.fiehnlab.mona.backend.services.statistics.server

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.{EurekaClientConfig, SwaggerConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.config.StatisticsRepositoryConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter, WebSecurityCustomizer}
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.annotation.web.builders.WebSecurity

@SpringBootApplication
@EnableWebSecurity
@EnableScheduling
@Order(3)
@Import(Array(classOf[JWTAuthenticationConfig], classOf[SwaggerConfig], classOf[EurekaClientConfig], classOf[StatisticsRepositoryConfig], classOf[PostgresqlConfiguration]))
class StatisticServer extends WebSecurityConfigurerAdapter {
  @Autowired
  val restSecurityService: RestSecurityService = null

  /**
   * this method configures authorized access to the system
   * and protects the urls with the specified methods and credentials
   *
   * @param http
   */
  override final def configure(http: HttpSecurity): Unit = {
    restSecurityService.prepare(http)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()

      //deletes need authentication
      .antMatchers(HttpMethod.DELETE).authenticated()

      //update statistics need authentication
      .antMatchers(HttpMethod.POST, "/rest/statistics/update").hasAuthority("ADMIN")
      .antMatchers(HttpMethod.POST, "/rest/spectra/refresh").hasAuthority("ADMIN")
  }

  /**
   * this method configures, which parts of the system and which methods do not need
   * any form of security in place and can be openly accessed
   *
   * @param web
   */
  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      .antMatchers(HttpMethod.GET, "/rest/tags/**")
      .antMatchers(HttpMethod.GET, "/rest/statistics/**")
      .antMatchers(HttpMethod.GET, "/rest/metaData/**")
      //no authentication for metadata
      .antMatchers(HttpMethod.POST, "/rest/metaData/**")
  }
}

object StatisticServer extends App {
  private val app = new SpringApplication(classOf[StatisticServer])
  app.run()
}