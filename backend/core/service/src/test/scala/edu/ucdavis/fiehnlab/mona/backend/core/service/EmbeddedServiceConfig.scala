package edu.ucdavis.fiehnlab.mona.backend.core.service

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.EmbeddedElasticSearchConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.service.config.PersistenceServiceConfig
import org.springframework.context.annotation.{Import, Configuration}

/**
  * Created by wohlg on 3/16/2016.
  */

@Configuration
@Import(Array(classOf[EmbeddedElasticSearchConfiguration],classOf[EmbeddedMongoDBConfiguration],classOf[PersistenceServiceConfig]))
class EmbeddedServiceConfig {

}
