package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.config

import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller.SpectrumRestController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.listener.{SpectrumElasticEventListener, PersistenceEvent}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.service.SpectrumPersistenceService
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.{Import, ComponentScan, Configuration}

/**
  * Created by wohlg on 3/15/2016.
  */
@Configuration
@Import(Array(classOf[ElasticsearchConfig]))
@ComponentScan(basePackageClasses = Array(classOf[SpectrumRestController],classOf[SpectrumPersistenceService],classOf[SpectrumElasticEventListener]))
class RestServerConfig {

}
