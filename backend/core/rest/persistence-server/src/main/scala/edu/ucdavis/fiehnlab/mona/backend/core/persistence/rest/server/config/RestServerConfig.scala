package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{MonaMapper}
import java.util
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.SwaggerConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import, Primary, Profile}
import org.springframework.core.annotation.Order
import org.springframework.http._
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter, WebSecurityCustomizer}
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.web.servlet.config.annotation.{ContentNegotiationConfigurer, PathMatchConfigurer, WebMvcConfigurer}
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.web.SecurityFilterChain
import th.co.geniustree.springdata.jpa.repository.support.JpaSpecificationExecutorWithProjectionImpl
/**
  * this class configures all our controller and also prepares security measures for these mentioned controllers
  */
@Configuration
@Import(Array(classOf[PostgresqlConfiguration], classOf[SwaggerConfig], classOf[SerializationConfig]))
@EnableAutoConfiguration
@ComponentScan(basePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller"))
@EnableWebSecurity
@Order(1)
@Profile(Array("mona.persistence"))
class RestServerConfig {

  @Autowired
  val restSecurityService: RestSecurityService = null

  @Bean
  def filterChain(http: HttpSecurity): SecurityFilterChain = {
    restSecurityService.prepare(http)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()

      //get on submitters is restricted
      .antMatchers(HttpMethod.GET, "/rest/submitters/**").authenticated()

      //saves need to be authenticated
      .antMatchers(HttpMethod.POST, "/rest/spectra/**").authenticated()
      .antMatchers(HttpMethod.POST, "/rest/submitters").authenticated()

      //updates needs authentication
      .antMatchers(HttpMethod.PUT, "/rest/spectra/**").authenticated()
      .antMatchers(HttpMethod.PUT, "/rest/submitters").authenticated()

      //news can only be added or updated by admins
      .antMatchers(HttpMethod.PUT, "/rest/news/**").hasAuthority("ADMIN")
      .antMatchers(HttpMethod.POST, "/rest/news/**").hasAuthority("ADMIN")

      //deletes need authentication
      .antMatchers(HttpMethod.DELETE).authenticated()

      //update statistics need authentication
      .antMatchers(HttpMethod.POST, "/rest/statistics/update").hasAuthority("ADMIN")
    http.build()
  }

  @Bean
  def webSecurityCustomizer(): WebSecurityCustomizer = {
      (web) => web.ignoring()
        //get is available for most endpoints
        .antMatchers(HttpMethod.GET, "/rest/spectra/**")
        .antMatchers(HttpMethod.GET, "/rest/metaData/**")
        .antMatchers(HttpMethod.GET, "/rest/tags/**")
        .antMatchers(HttpMethod.GET, "/rest/statistics/**")
        .antMatchers(HttpMethod.GET, "/rest/news/**")
        .antMatchers(HttpMethod.GET, "/rest/feedback/**")

        .antMatchers(HttpMethod.POST, "/rest/feedback")
        .antMatchers(HttpMethod.POST, "/rest/spectra/count")

        //no authentication for metadata
        .antMatchers(HttpMethod.POST, "/rest/metaData/**")

  }
}

@Configuration
class SerializationConfig extends WebMvcConfigurer with LazyLogging {

  override def extendMessageConverters(converters: util.List[HttpMessageConverter[_]]): Unit = {
    converters.add(new MSPConverter())
    converters.add(new SDFConverter())
    converters.add(new PNGConverter())
  }

  override def configurePathMatch(configurer: PathMatchConfigurer): Unit = {
    configurer.setUseRegisteredSuffixPatternMatch(true)
  }

  override def configureContentNegotiation(configurer: ContentNegotiationConfigurer): Unit = {
    configurer.favorPathExtension(false).
      favorParameter(true).
      parameterName("mediaType").
      ignoreAcceptHeader(false).
      useRegisteredExtensionsOnly(true)
      .defaultContentType(MediaType.APPLICATION_JSON)
      .mediaType("msp", MediaType.valueOf("txt/msp"))
      .mediaType("sdf", MediaType.valueOf("txt/sdf"))
      .mediaType("json", MediaType.APPLICATION_JSON)

    super.configureContentNegotiation(configurer)
  }
}
