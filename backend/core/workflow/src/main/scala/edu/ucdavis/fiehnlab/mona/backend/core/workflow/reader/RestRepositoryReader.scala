package edu.ucdavis.fiehnlab.mona.backend.core.workflow.reader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.{GenericRestClient, MonaSpectrumRestClient}
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.beans.BeanProperty

/**
  * connects to the mona server
  * and reads spectra from it utilzing the rest apik
  */
class RestRepositoryReader(@BeanProperty val query: String = "",@BeanProperty val pageSize: Int = 10) extends ItemReader[Spectrum] with LazyLogging {


  @Autowired
  val spectrumRestClient: GenericRestClient[Spectrum, String] = null

  /**
    * query the results for us, based on the given query
    */
  var result: Iterator[Spectrum] = null

  /**
    * reads the next available spectrum or returns null
    *
    * @return
    */
  override def read(): Spectrum = {

    if (result == null) {
      logger.debug("connecting to rest server and fetch data")
      if (query == "") {
        result = spectrumRestClient.stream(None, Some(pageSize)).iterator
      }
      else {
        result = spectrumRestClient.stream(Some(query), Some(pageSize)).iterator
      }
    }
    if (result.hasNext) {

      //fetch the next results
      result.next()
    }
    else {
      //as defined by spring batch standards
      null
    }
  }
}
