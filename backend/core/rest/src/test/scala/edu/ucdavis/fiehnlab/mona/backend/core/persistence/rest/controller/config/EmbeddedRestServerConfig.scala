package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller.config

import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.EmbeddedElasticSearchConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller.GenericRESTController
import org.springframework.context.annotation.{ComponentScan, Import, Configuration}

/**
  * Created by wohlg on 3/11/2016.
  */
@Configuration
@Import(Array(classOf[EmbeddedMongoDBConfiguration],classOf[EmbeddedElasticSearchConfiguration],classOf[DomainConfig]))
@ComponentScan(basePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller"))
class EmbeddedRestServerConfig {

}
