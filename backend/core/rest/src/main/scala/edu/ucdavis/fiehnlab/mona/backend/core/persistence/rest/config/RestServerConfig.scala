package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller.SpectrumRestController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.listener.{SpectrumElasticEventListener, PersistenceEvent}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.service.SpectrumPersistenceService
import org.springframework.context.annotation.{ComponentScan, Configuration}

/**
  * Created by wohlg on 3/15/2016.
  */
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[SpectrumRestController],classOf[SpectrumPersistenceService],classOf[SpectrumElasticEventListener]))
class RestServerConfig {

}
