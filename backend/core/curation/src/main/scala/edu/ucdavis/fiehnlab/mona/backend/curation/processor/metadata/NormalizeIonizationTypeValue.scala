package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.common.{CommonMetaData, MetaDataSynonyms}
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 4/16/16.
  */
@Step(description = "this step will update ionization type metadata values to standard values")
class NormalizeIonizationTypeValue extends ItemProcessor[Spectrum,Spectrum] with LazyLogging {
  val POSITIVE_TERMS: Array[String] = Array("positive", "pos", "p", "+")
  val NEGATIVE_TERMS: Array[String] = Array("negative", "neg", "n", "-")

  /**
    * processes the given spectrum and removes all it's computed meta data
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    //assemble updated spectrum
    spectrum.copy(
      metaData = spectrum.metaData.map(normalizeIonModeData).filter(_!= null)
    )
  }

  /**
    * rename metadata names according to a provided synonym list
    *
    * @param metaData
    * @return
    */
  def normalizeIonModeData(metaData: MetaData) : MetaData = {
    if (metaData.name == CommonMetaData.IONIZATION_TYPE) {
      val value: String = metaData.value.toString.toLowerCase.trim

      logger.debug(s"Found ionization type metadata with value: $value")

      if (value == "n/a") {
        logger.warn(s"Ionization type with value '$value' deemed invalid - removing metadata value")
        null
      }

      else if (value == "positive" || value == "negative") {
        logger.debug(s"Ionization type with value 'value' requires no modifications")
        metaData
      }

      else if(POSITIVE_TERMS.contains(value)) {
        logger.info(s"Identified ionization type 'value' as positive mode")
        metaData.copy(value = "positive")
      }

      else if(NEGATIVE_TERMS.contains(value)) {
        logger.info(s"Identified ionization type 'value' as negative mode")
        metaData.copy(value = "negative")
      }

      else {
        logger.warn(s"Ionization type value 'value' was unidentifiable - keeping metadata value")
        metaData
      }
    } else {
      metaData
    }
  }
}