package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.GenericRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.EmbeddedRestServerConfig
import org.springframework.context.annotation.{Bean, Import}

/**
  * Created by wohlgemuth on 3/15/16.
  */
@Import(Array(classOf[RestClientConfig],classOf[EmbeddedRestServerConfig]))
class RestClientTestConfig{

  @Bean
  def spectrumRestClient: GenericRestClient[Spectrum, String] = {
    new GenericRestClient[Spectrum, String]("rest/spectra")
  }
}