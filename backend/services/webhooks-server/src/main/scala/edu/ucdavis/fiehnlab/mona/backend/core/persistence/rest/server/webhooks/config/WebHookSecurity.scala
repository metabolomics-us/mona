package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.config

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.SwaggerConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller.WebhookController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.listener.WebHookEventBusListener
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.{WebHookRepository, WebHookResultRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service.WebHookService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{ComponentScan, Configuration, Import}
import org.springframework.core.annotation.Order
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableMongoRepositories(basePackageClasses = Array(classOf[WebHookRepository],classOf[WebHookResultRepository]))
@ComponentScan(basePackageClasses = Array(classOf[WebhookController],classOf[WebHookRepository],classOf[WebHookService],classOf[WebHookEventBusListener]))
@Import(Array(classOf[MonaEventBusConfiguration],classOf[MonaNotificationBusConfiguration],classOf[SwaggerConfig]))
@Order(1)
class WebHookSecurity extends WebSecurityConfigurerAdapter {

  @Autowired
  val restSecurityService:RestSecurityService = null

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

      // listing webhooks is limited to authenticated users
      .antMatchers(HttpMethod.GET, "/rest/webhooks").authenticated()

      // manual triggering need to be authenticated as admin
      .antMatchers(HttpMethod.POST, "/rest/webhooks/push").hasAuthority("ADMIN")
      .antMatchers(HttpMethod.POST, "/rest/webhooks/pull").hasAuthority("ADMIN")
      .antMatchers(HttpMethod.POST, "/rest/webhooks/trigger/**").hasAuthority("ADMIN")

      // saves and updates needs authentication
      .antMatchers(HttpMethod.POST, "/rest/webhooks/**").authenticated()
      .antMatchers(HttpMethod.PUT).authenticated()

      //deletes need authentication
      .antMatchers(HttpMethod.DELETE).hasAuthority("ADMIN")
  }

  /**
    * this method configures, which parts of the system and which methods do not need
    * any form of security in place and can be openly accessed
    *
    * @param web
    */
  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      // sync should be publicly accessible
      .antMatchers(HttpMethod.GET, "/rest/webhooks/sync/**")

      // the test webhook should be publicly accessible
      .antMatchers(HttpMethod.GET, "/info/**")
  }
}
