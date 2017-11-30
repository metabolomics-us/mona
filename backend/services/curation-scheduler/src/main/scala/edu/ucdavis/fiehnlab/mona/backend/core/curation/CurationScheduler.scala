package edu.ucdavis.fiehnlab.mona.backend.core.curation

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.SwaggerConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.config.CurationConfig
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.web.client.{RestOperations, RestTemplate}
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
  * This class starts the curation service and let's it listen in the background for messages
  * it also exposes a couple of rest points, which allow simple scheduling of messages
  */
@SpringBootApplication
@EnableDiscoveryClient
@EnableWebSecurity
@EnableSwagger2
@Order(5)
@Import(Array(classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration], classOf[MongoConfig],
  classOf[JWTAuthenticationConfig], classOf[CurationConfig], classOf[SwaggerConfig]))
class CurationScheduler extends WebSecurityConfigurerAdapter {

  @Autowired
  val restSecurityService: RestSecurityService = null

  /**
    * rest operations interface, configured with a custom object mapper
    *
    * @return
    */
  @Bean
  def restOperations: RestOperations = {
    val httpClient = HttpClientBuilder.create().build()

    val converter: MappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter()
    converter.setObjectMapper(MonaMapper.create)

    val rest: RestTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient))
    rest.getMessageConverters.add(0, converter)
    rest
  }


  /**
    * only admins can schedule curations in the system
    *
    * @param http
    */
  override final def configure(http: HttpSecurity): Unit = {
    restSecurityService.prepare(http)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()
      .antMatchers(HttpMethod.GET, "/rest/curation/**").hasAuthority("ADMIN")
  }

  /**
    * any other get request is ignored by default
    * since we have /info etc exposed
    *
    * @param web
    */
  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      .antMatchers(HttpMethod.GET, "/*")
      .antMatchers(HttpMethod.POST, "/rest/curation")
  }
}

/**
  * our local server, which should be connecting to eureka, etc
  */
object CurationScheduler extends App {
  new SpringApplication(classOf[CurationScheduler]).run()
}
