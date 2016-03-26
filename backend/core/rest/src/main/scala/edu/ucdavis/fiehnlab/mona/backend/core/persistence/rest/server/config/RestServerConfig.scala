package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import edu.ucdavis.fiehnlab.mona.backend.core.service.config.PersistenceServiceConfig
import org.springframework.context.annotation.{ComponentScan, Configuration, Import}
/**
  * Created by wohlg on 3/15/2016.
  */
@Configuration
@Import(Array(classOf[PersistenceServiceConfig]))
@ComponentScan(basePackageClasses = Array(classOf[GenericRESTController[Spectrum]]))
class RestServerConfig {

}

