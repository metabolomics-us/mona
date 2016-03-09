package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.stereotype.Component

/**
  * specific client to work with MoNA spectrums and there MetaData
  */
@Component
class MonaSpectrumRestClient(val server: String) extends GenericRestClient[Spectrum, String](s"rest/spectrum") {

  val metaDataPath = "/rest/metaData"
  /**
    * returns a list of all available metadata names
    */
  def listMetaDataNames : Array[String] = {
    restOperations.getForObject(s"$server/$metaDataPath/names",classOf[Array[String]])
  }

  /**
    * gets all available metadata values for the given name
    *
    * @param name
    */
  def listMetaDataValues(name: String):Array[Any] = {
    restOperations.getForObject(s"$server/$metaDataPath/values/$name",classOf[Array[Any]])
  }

}
