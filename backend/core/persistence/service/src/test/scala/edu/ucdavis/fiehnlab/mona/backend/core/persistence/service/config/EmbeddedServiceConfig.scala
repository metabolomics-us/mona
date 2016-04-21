package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.config

import org.springframework.context.annotation.{Configuration, Import}

/**
  * Created by wohlg on 3/16/2016.
  */

@Configuration
@Import(Array(classOf[PersistenceServiceConfig]))
class EmbeddedServiceConfig {

}
