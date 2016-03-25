package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.config.AuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.EmbeddedElasticSearchConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import org.springframework.context.annotation.{Primary, Bean, Configuration, Import}
import org.springframework.security.authentication._

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.{Authentication}
import scala.collection.JavaConverters._

/**
  * Created by wohlg on 3/11/2016.
  */
@Configuration
@Import(Array(classOf[EmbeddedElasticSearchConfiguration], classOf[EmbeddedMongoDBConfiguration], classOf[RestServerConfig],classOf[AuthenticationConfig]))
class EmbeddedRestServerConfig extends LazyLogging {

  @Bean
  @Primary
  def authenticationManager(authenticationProvider:AuthenticationProvider) : AuthenticationManager = {
    logger.debug(s"utilizing: ${authenticationProvider}")
    new ProviderManager(List[AuthenticationProvider](authenticationProvider).asJava)
  }
}
