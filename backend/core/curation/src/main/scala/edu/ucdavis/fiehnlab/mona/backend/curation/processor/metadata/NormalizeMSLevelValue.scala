package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.common.CommonMetaData
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 4/16/16.
  */
@Step(description = "this step will update ms level metadata values to standard values")
class NormalizeMSLevelValue extends ItemProcessor[Spectrum,Spectrum] with LazyLogging {
  /**
    * processes the given spectrum and removes all it's computed meta data
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    //assemble updated spectrum
    spectrum.copy(
      metaData = spectrum.metaData.map(normalizeMSLevelData).filter(_!= null)
    )
  }

  /**
    * rename metadata names according to a provided synonym list
    *
    * @param metaData
    * @return
    */
  def normalizeMSLevelData(metaData: MetaData) : MetaData = {
    if (metaData.name == CommonMetaData.MS_LEVEL) {
      val value: String = metaData.value.toString.trim

      logger.debug(s"Found MS level metadata with value: $value")

      if (value == "n/a") {
        logger.warn(s"MS level with value '$value' deemed invalid - removing metadata value")
        null
      }

      else if ("^MS[1-9]$".r.findFirstIn(value).isDefined) {
        logger.debug(s"MS level with value '$value' requires no modifications")
        metaData
      }

      else if (value.toLowerCase == "ms") {
        logger.info(s"Identified MS level value '$value' as MS1")
        metaData.copy(value = "MS1")
      }

      else if (value.toLowerCase == "msms" || value.toLowerCase == "ms/ms") {
        logger.info(s"Identified MS level value '$value' as MS2")
        metaData.copy(value = "MS2")
      }

      else if ("^[1-9]$".r.findFirstIn(value).isDefined) {
        logger.info(s"Identified MS level value '$value' as MS$value")
        metaData.copy(value = s"MS$value")
      }

      else {
        logger.warn(s"MS level value '$value' was unidentifiable - keeping metadata value")
        metaData
      }
    } else {
      metaData
    }
  }
}