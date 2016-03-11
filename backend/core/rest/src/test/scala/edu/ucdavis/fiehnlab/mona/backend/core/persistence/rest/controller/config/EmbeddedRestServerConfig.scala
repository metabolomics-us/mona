package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.EmbeddedElasticSearchConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import org.springframework.context.annotation.{Import, Configuration}

/**
  * Created by wohlg on 3/11/2016.
  */
@Configuration
@Import(Array(classOf[EmbeddedMongoDBConfiguration],classOf[EmbeddedElasticSearchConfiguration]))
class EmbeddedRestServerConfig {

}
