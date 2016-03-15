package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller.config.EmbeddedRestServerConfig
import org.springframework.context.annotation.Import

/**
  * Created by wohlgemuth on 3/15/16.
  */
@Import(Array(classOf[RestClientConfig],classOf[EmbeddedRestServerConfig]))
class RestClientTestConfig