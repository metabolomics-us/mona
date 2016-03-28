package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.EmbeddedElasticSearchConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import org.springframework.context.annotation.{Primary, Bean, Configuration, Import}
import org.springframework.security.authentication._
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.{GrantedAuthority, Authentication}
import scala.collection.JavaConverters._

/**
  * Created by wohlg on 3/11/2016.
  */
@Configuration
@Import(Array(classOf[EmbeddedElasticSearchConfiguration], classOf[EmbeddedMongoDBConfiguration], classOf[RestServerConfig]))
class EmbeddedRestServerConfig extends LazyLogging {

  @Bean
  @Primary
  def authentificationManager: AuthenticationManager = {
    logger.info("creating custom authentification manager")
    new ProviderManager(List[AuthenticationProvider](new TestAuthentificationProvider()).asJava)
  }
}

/**
  * mocks our authentification manager for us
  */
class TestAuthentificationProvider extends AuthenticationProvider with LazyLogging {
  override def authenticate(authentication: Authentication): Authentication = {

    logger.debug(s"attempting login with ${authentication.getName}")
    val name = authentication.getName
    val password = authentication.getCredentials.toString

    if (name == "admin" && password == "secret") {
      new UsernamePasswordAuthenticationToken(name, password, List(new SimpleGrantedAuthority("ADMIN")).asJava)
    }

    else if (name == "test" && password == "test-secret") {
      new UsernamePasswordAuthenticationToken(name, password, List(new SimpleGrantedAuthority("USER")).asJava)
    }
    else {
      throw new BadCredentialsException("1000")
    }
  }

  override def supports(authentication: Class[_]): Boolean = {
    return authentication.equals(classOf[UsernamePasswordAuthenticationToken])

  }
}