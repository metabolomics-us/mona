package edu.ucdavis.fiehnlab.mona.core.similarity

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.{EurekaClientConfig, SwaggerConfig}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.security.config.http.SessionCreationPolicy


/**
  * Created by sajjan on 10/11/16.
  */
@SpringBootApplication
@EnableWebSecurity
@Order(5)
@Import(Array(classOf[MongoConfig], classOf[JWTAuthenticationConfig], classOf[SwaggerConfig], classOf[EurekaClientConfig]))
class SimilarityService extends WebSecurityConfigurerAdapter {

  @Autowired
  val restSecurityService: RestSecurityService = null

  /**
    *
    * @param http
    */
  override final def configure(http: HttpSecurity): Unit = {
    restSecurityService.prepare(http)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
  }

  /**
    *
    * @param web
    */
  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      .antMatchers(HttpMethod.GET)
      .antMatchers(HttpMethod.POST, "/rest/similarity/*")
  }
}

/**
  * our local server, which should be connecting to eureka, etc
  */
object SimilarityService extends App {
  new SpringApplication(classOf[SimilarityService]).run()
}
