package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsMetaData
import org.springframework.http.{HttpEntity, HttpMethod}
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

import java.net.{URI, URLEncoder}

/**
  * specific client to work with MoNA spectrums and there MetaData
  */
@Component
class MonaSpectrumRestClient extends GenericRestClient[Spectrum, String](s"rest/spectra") {

  val metaDataPath = "rest/metaData"

  /**
    * returns a list of all available metadata names
    */
  def listMetaDataNames: Array[String] = {
    restOperations.getForObject(s"$monaRestServer/$metaDataPath/names", classOf[Array[StatisticsMetaData]]).map(_.getName)
  }

  /**
    * gets all available metadata values for the given name
    *
    * @param name
    */
  def listMetaDataValues(name: String): Array[StatisticsMetaData] = {
    val builder: UriComponentsBuilder = UriComponentsBuilder.fromUriString(s"$monaRestServer/$metaDataPath/values")
      .queryParam("name", URLEncoder.encode(name, "UTF-8"))
    val uri: URI = builder.build(true).toUri
    restOperations.getForObject(uri, classOf[Array[StatisticsMetaData]])
  }

  /**
    * regenerates all the spectrum related statistics
    *
    * @return
    */
  def regenerateStatistics(): Unit = {
    restOperations.postForEntity(s"${this.monaRestServer}/rest/statistics/update", new HttpEntity[String]("", this.buildHeaders), classOf[Void])
  }

  def regenerateDownloads(): Unit = {
    val url = s"${this.monaRestServer}/rest/downloads/generatePredefined"

    restOperations.exchange(url, HttpMethod.GET, new HttpEntity[String]("parameters", this.buildHeaders), classOf[Array[Any]])
  }
}
