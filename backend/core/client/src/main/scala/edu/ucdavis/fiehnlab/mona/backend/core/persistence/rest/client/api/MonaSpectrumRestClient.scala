package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.springframework.http.{HttpEntity, HttpMethod}
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
  def listMetaDataNames: Array[String] = {
    restOperations.getForObject(s"$monaRestServer/$metaDataPath/names", classOf[Array[MetaDataName]]).map(_.name)
  }

  /**
    * gets all available metadata values for the given name
    *
    * @param name
    */
  def listMetaDataValues(name: String): Array[Any] = {
    restOperations.getForObject(s"$monaRestServer/$metaDataPath/values?name=$name", classOf[Array[Any]])
  }

  /**
    * regenerates all the spectrum related statistics
    * @return
    */
  def regenerateStatistics = {
    restOperations.exchange(s"${this.monaRestServer}/rest/statistics/update",HttpMethod.POST,new HttpEntity[String]("",this.buildHeaders),classOf[String])
  }
}

case class MetaDataName(name: String, count: Int)