package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.config

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.EmbeddedElasticSearchConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.listener.AkkaEventScheduler
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, Configuration, Import}

/**
  * Created by wohlg on 3/16/2016.
  */

@Configuration
@Import(Array(classOf[EmbeddedElasticSearchConfiguration],classOf[PersistenceServiceConfig]))
class EmbeddedServiceConfig {

}
