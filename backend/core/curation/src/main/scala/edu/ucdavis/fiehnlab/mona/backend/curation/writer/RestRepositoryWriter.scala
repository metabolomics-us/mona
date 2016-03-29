package edu.ucdavis.fiehnlab.mona.backend.curation.writer

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 3/11/16.
  */
@Component
class RestRepositoryWriter extends WriterAdapter with LazyLogging {

  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null

  /**
    * attempts to write all these spectra to the repository
    * and saves them if they have an id or updates them if they do not have an id
    *
    * @param spectrum
    */
  override def write(spectrum: Spectrum): Unit = {

    if (spectrum.id == null) {
      logger.debug("adding spectra to server")
      monaSpectrumRestClient.add(spectrum)
    }
    else {
      logger.debug(s"updating spectra on server ${spectrum.id}")
      monaSpectrumRestClient.update(spectrum, spectrum.id)
    }

  }
}
