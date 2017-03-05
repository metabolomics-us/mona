package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Impact, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CommonTags}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.ArrayBuffer

/**
  * Created by sajjan on 3/3/17.
  */
@Step(description = "this step will identify required metadata fields and add impact values accordingly")
class IdentifyMetaDataFields extends ItemProcessor[Spectrum,Spectrum] with LazyLogging {

  /**
    * processes the given spectrum and removes all it's computed meta data
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {

    val metaDataNames: Array[String] = spectrum.metaData.map(_.name.toLowerCase)
    val tagNames: Array[String] = spectrum.tags.map(_.text.toLowerCase)
    val impacts: ArrayBuffer[Impact] = new ArrayBuffer[Impact]()

    // Check for instrument type
    if (metaDataNames.contains(CommonMetaData.INSTRUMENT.toLowerCase) || metaDataNames.contains(CommonMetaData.INSTRUMENT_TYPE.toLowerCase)) {
      impacts.append(Impact(1, "Instrument information provided"))
    } else {
      impacts.append(Impact(-1, "No instrument information provided"))
    }

    // Check for collision energy
    if (metaDataNames.contains(CommonMetaData.COLLISION_ENERGY.toLowerCase)) {
      impacts.append(Impact(1, "Collision energy provided"))
    } else {
      impacts.append(Impact(-1, "No collision energy provided"))
    }

    if (tagNames.contains(CommonTags.LCMS_SPECTRUM.toLowerCase) || tagNames.contains(CommonTags.GCMS_SPECTRUM.toLowerCase)) {
      // Check for retention time/index
      if (metaDataNames.contains(CommonMetaData.RETENTION_TIME.toLowerCase) || metaDataNames.contains(CommonMetaData.RETENTION_INDEX.toLowerCase)) {
        impacts.append(Impact(1, "Retention time/index provided"))

        // Check for column information
        if (metaDataNames.contains(CommonMetaData.COLUMN.toLowerCase)) {
          impacts.append(Impact(1, "Column information provided"))
        } else {
          impacts.append(Impact(-1, "No column information provided"))
        }
      } else {
        impacts.append(Impact(-1, "No retention time/index provided"))
      }
    }

    // Check for precursor information
    if (tagNames.contains(CommonTags.LCMS_SPECTRUM.toLowerCase)) {
      if (metaDataNames.contains(CommonMetaData.PRECURSOR_TYPE.toLowerCase)) {
        impacts.append(Impact(1, "Precursor type provided"))
      } else {
        impacts.append(Impact(1, "No precursor type provided"))
      }

      if (metaDataNames.contains(CommonMetaData.PRECURSOR_MASS.toLowerCase)) {
        impacts.append(Impact(1, "Precursor m/z provided"))
      } else {
        impacts.append(Impact(1, "No precursor m/z provided"))
      }
    }

    // Return array with added impact values
    spectrum.copy(score = spectrum.score.copy(impacts = spectrum.score.impacts ++ impacts))
  }
}
