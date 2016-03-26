package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, JWTRestSecurityConfig}
import org.springframework.context.annotation.Import

/**
  * Created by wohlgemuth on 3/15/16.
  */
@Import(Array(classOf[RestClientConfig],classOf[EmbeddedRestServerConfig],classOf[JWTRestSecurityConfig]))
class RestClientTestConfig