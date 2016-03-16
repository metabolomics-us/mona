package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.SpectrumRestController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.listener.{SpectrumElasticEventListener}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.service.SpectrumPersistenceService
import org.springframework.context.annotation.{Import, ComponentScan, Configuration}

/**
  * Created by wohlg on 3/15/2016.
  */
@Configuration
@Import(Array(classOf[ElasticsearchConfig]))
@ComponentScan(basePackageClasses = Array(classOf[SpectrumRestController],classOf[SpectrumPersistenceService],classOf[SpectrumElasticEventListener]))
class RestServerConfig {

}
