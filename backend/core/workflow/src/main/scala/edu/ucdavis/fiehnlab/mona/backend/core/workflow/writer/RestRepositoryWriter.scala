package edu.ucdavis.fiehnlab.mona.backend.core.workflow.writer

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.GenericRestClient
import org.springframework.batch.item.ItemWriter
import org.springframework.beans.factory.annotation.Autowired
import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/11/16.
  */
class RestRepositoryWriter extends ItemWriter[Spectrum] with LazyLogging{

  @Autowired
  val spectrumRestClient: GenericRestClient[Spectrum, String] = null

  /**
    * attempts to write all these spectra to the repository
    * and saves them if they have an id or updates them if they do not have an id
    *
    * @param items
    */
  override def write(items: util.List[_ <: Spectrum]): Unit = {
    for(spectrum <- items.asScala){
      if(spectrum.id == null){
        logger.debug("adding spectra to server")
        spectrumRestClient.add(spectrum)
      }
      else{
        logger.debug(s"updating spectra on server ${spectrum.id}")
        spectrumRestClient.update(spectrum,spectrum.id)
      }
    }
  }
}
