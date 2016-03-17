package edu.ucdavis.fiehnlab.mona.backend.core.service.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.service.persistence.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.mona.backend.core.service.synchronization.SpectrumElasticEventListener
import org.springframework.context.annotation.{Import, Configuration, ComponentScan}

/**
  * Created by wohlg on 3/16/2016.
  */
@ComponentScan(basePackageClasses = Array(classOf[SpectrumPersistenceService], classOf[SpectrumElasticEventListener]))
@Configuration
@Import(Array(classOf[ElasticsearchConfig],classOf[MongoConfig]))
class PersistenceServiceConfig {

}