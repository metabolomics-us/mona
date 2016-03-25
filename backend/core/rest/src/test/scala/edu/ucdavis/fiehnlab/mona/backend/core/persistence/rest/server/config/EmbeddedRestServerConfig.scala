package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.config.{AuthenticationConfig, EmbeddedAuthConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.{LoginService, MongoLoginService}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.EmbeddedElasticSearchConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import org.springframework.context.annotation.{Bean, Configuration, Import, Primary}
import org.springframework.security.authentication._
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.Authentication

import scala.collection.JavaConverters._

/**
  * Created by wohlg on 3/11/2016.
  */
@Configuration
@Import(Array(classOf[EmbeddedElasticSearchConfiguration], classOf[EmbeddedMongoDBConfiguration], classOf[RestServerConfig],classOf[AuthenticationConfig],classOf[EmbeddedAuthConfig]))
class EmbeddedRestServerConfig extends LazyLogging {

}
