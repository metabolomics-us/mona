package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.WrappedString
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.springframework.stereotype.Component

/**
  * specific client to work with MoNA spectrums and there MetaData
  */
@Component
class MonaSpectrumRestClient extends GenericRestClient[Spectrum, String](s"rest/spectra") {

  val metaDataPath = "/rest/metaData"
  /**
    * returns a list of all available metadata names
    */
  def listMetaDataNames : Array[String] = {
    restOperations.getForObject(s"$monaRestServer/$metaDataPath/names", classOf[Array[String]])
  }

  /**
    * gets all available metadata values for the given name
    *
    * @param name
    */
  def listMetaDataValues(name: String):Array[Any] = {
    val url = s"$monaRestServer/$metaDataPath/values"
    restOperations.postForObject(url,WrappedString(name), classOf[Array[Any]])
  }
}
