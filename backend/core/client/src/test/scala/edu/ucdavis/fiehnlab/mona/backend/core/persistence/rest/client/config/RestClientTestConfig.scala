package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.service.RestLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.RestServerConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.config.StatisticsRepositoryConfig
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import, Primary}
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

/**
  * Created by wohlgemuth on 3/15/16.
  */
@Import(Array(classOf[RestClientConfig], classOf[EmbeddedRestServerConfig], classOf[AuthSecurityConfig], classOf[StatisticsRepositoryConfig]))
@SpringBootApplication
class RestClientTestConfig {

  @Bean
  @Primary
  def loginService(@Value("${mona.rest.server.host}") host: String, @Value("${mona.rest.server.port}") port: Int): LoginService =
    new RestLoginService("localhost", port)

  @Bean
  def loginServiceDelegate: LoginService = new PostgresLoginService
}

@Configuration
@Import(Array(classOf[RestServerConfig], classOf[StatisticsControllersConfig]))
class EmbeddedRestServerConfig extends LazyLogging {

  /**
   * the service which actually does the login for us
   *
   * @return
   */
  @Bean
  def loginService: LoginService = new PostgresLoginService

}

/**
  * Wires up just the statistics-server REST controllers this test talks to (StatisticsRestController,
  * MetaDataRestController) and their security rules, without importing the full StatisticServer
  * @SpringBootApplication. Combining StatisticServer's @EnableAsync with RestServerConfig's
  * SpectrumRestController/SubmitterRestController (which autowire HttpServletRequest) in one
  * ApplicationContext causes those two controller beans to silently vanish from the MVC handler
  * mapping. Since this test embeds two independently-deployed microservices' Spring contexts into a
  * single JVM only for its own convenience, scanning just the controllers avoids the conflict entirely.
  * StatisticsUpdateRunner.runUpdate()'s @Async has no effect without @EnableAsync, so it just runs
  * synchronously here, which is fine (arguably more deterministic) for this test
  */
@Configuration
@ComponentScan(basePackages = Array(
  "edu.ucdavis.fiehnlab.mona.backend.services.statistics.server.controller",
  "edu.ucdavis.fiehnlab.mona.backend.services.statistics.server.service"
))
@Order(3)
class StatisticsControllersConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  val restSecurityService: RestSecurityService = null

  override final def configure(http: HttpSecurity): Unit = {
    restSecurityService.prepare(http)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()
      .antMatchers(HttpMethod.POST, "/rest/statistics/update").hasAuthority("ADMIN")
      .antMatchers(HttpMethod.POST, "/rest/tags/library/refresh").hasAuthority("ADMIN")
      .antMatchers(HttpMethod.POST, "/rest/spectra/refresh").hasAuthority("ADMIN")
  }

  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      .antMatchers(HttpMethod.GET, "/rest/tags/**")
      .antMatchers(HttpMethod.GET, "/rest/statistics/**")
      .antMatchers(HttpMethod.GET, "/rest/metaData/**")
      .antMatchers(HttpMethod.POST, "/rest/metaData/**")
  }
}
