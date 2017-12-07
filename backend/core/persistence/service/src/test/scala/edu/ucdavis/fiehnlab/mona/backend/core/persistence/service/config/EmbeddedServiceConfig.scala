package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.config

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.MonaEventBusCounterConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Configuration, Import}

/**
  * Created by wohlg on 3/16/2016.
  */

@SpringBootApplication
@Import(Array(classOf[PersistenceServiceConfig], classOf[MonaEventBusCounterConfiguration]))
class EmbeddedServiceConfig