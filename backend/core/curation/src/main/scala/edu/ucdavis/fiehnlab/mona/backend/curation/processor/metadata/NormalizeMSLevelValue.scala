package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Score, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
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
    val updatedMetaData: Array[MetaData] = spectrum.metaData.map(normalizeMSLevelData(_, spectrum.id)).filter(_ != null)

    val updatedScore: Score =
      if (updatedMetaData.exists(x => x.name == CommonMetaData.MS_LEVEL && x.value.toString.matches("^MS[1-9]$"))) {
        CurationUtilities.addImpact(spectrum.score, 1, "MS type/level identified")
      } else {
        CurationUtilities.addImpact(spectrum.score, -1, "No MS type/level provided")
      }

    spectrum.copy(
      metaData = updatedMetaData,
      score = updatedScore
    )
  }

  /**
    * rename metadata names according to a provided synonym list
    *
    * @param metaData
    * @return
    */
  def normalizeMSLevelData(metaData: MetaData, id: String): MetaData = {
    if (metaData.name == CommonMetaData.MS_LEVEL) {
      val value: String = metaData.value.toString.trim

      logger.debug(s"$id: Found MS level metadata with value: $value")

      if (value == "n/a") {
        logger.warn(s"$id: MS level with value '$value' deemed invalid - removing metadata value")
        null
      }

      else if ("^MS[1-9]$".r.findFirstIn(value).isDefined) {
        logger.info(s"$id: MS level with value '$value' requires no modifications")
        metaData
      }

      else if ("^ms[1-9]$".r.findFirstIn(value.toLowerCase()).isDefined) {
        logger.info(s"$id: Identified MS level value '$value' as ${value.toUpperCase}")
        metaData.copy(value = value.toUpperCase)
      }

      else if (value.toLowerCase == "ms") {
        logger.info(s"$id: Identified MS level value '$value' as MS1")
        metaData.copy(value = "MS1")
      }

      else if (value.toLowerCase == "msms" || value.toLowerCase == "ms/ms") {
        logger.info(s"$id: Identified MS level value '$value' as MS2")
        metaData.copy(value = "MS2")
      }

      else if ("^[1-9]$".r.findFirstIn(value).isDefined) {
        logger.info(s"$id: Identified MS level value '$value' as MS$value")
        metaData.copy(value = s"MS$value")
      }

      else {
        logger.warn(s"$id: MS level value '$value' was unidentifiable - keeping metadata value")
        metaData
      }
    } else {
      metaData
    }
  }
}