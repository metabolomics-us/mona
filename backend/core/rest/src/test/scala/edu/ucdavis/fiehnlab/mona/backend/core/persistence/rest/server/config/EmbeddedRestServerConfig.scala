package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.EmbeddedElasticSearchConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import org.springframework.context.annotation.{Configuration, Import}

/**
  * Created by wohlg on 3/11/2016.
  */
@Configuration
@Import(Array(classOf[EmbeddedElasticSearchConfiguration],classOf[EmbeddedMongoDBConfiguration],classOf[RestServerConfig]))
class EmbeddedRestServerConfig