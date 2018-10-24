package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.springframework.http.{HttpEntity, HttpMethod}
import org.springframework.stereotype.Component

/**
  * client to work with MoNA spectra and associated metaaata
  */
@Component
class MonaSpectrumRestClient extends GenericRestClient[Spectrum, String](s"rest/spectra") {

  /**
    * returns a list of all available metadata names
    */
  def listMetaDataNames: Array[String] = {
    restOperations.getForObject(s"$monaRestServer/rest/metaData/names", classOf[Array[MetaDataName]]).map(_.name)
  }

  /**
    * gets all available metadata values for the given name
    *
    * @param name
    */
  def listMetaDataValues(name: String): Array[Any] = {
    restOperations.getForObject(s"$monaRestServer/rest/metaData/values?name={name}", classOf[Array[Any]], name)
  }

  /**
    * regenerates all the spectrum related statistics
    *
    * @return
    */
  def regenerateStatistics(): Unit = {
    restOperations.postForEntity(s"$monaRestServer/rest/statistics/update", new HttpEntity[String]("", this.buildHeaders), classOf[Void])
  }

  /**
    * regenerates all pre-defined downloads
    */
  def regenerateDownloads(): Unit = {
    restOperations.exchange(s"$monaRestServer/rest/downloads/generatePredefined", HttpMethod.GET,
      new HttpEntity[String]("parameters", this.buildHeaders), classOf[Array[Any]])
  }
}

case class MetaDataName(name: String, count: Int)