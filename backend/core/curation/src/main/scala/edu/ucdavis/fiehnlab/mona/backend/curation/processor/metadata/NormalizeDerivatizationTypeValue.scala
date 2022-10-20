package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{MetaDataDAO, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.{Buffer}
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 4/16/16.
  */
@Step(description = "this step will update derivatization type metadata values to standard values")
class NormalizeDerivatizationTypeValue extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {

    val metaData: Buffer[MetaDataDAO] = spectrum.getMetaData.asScala
    val matches: Buffer[MetaDataDAO] = metaData.filter(_.getName.toLowerCase == CommonMetaData.DERIVATIZATION_TYPE.toLowerCase)

    null
  }
}
