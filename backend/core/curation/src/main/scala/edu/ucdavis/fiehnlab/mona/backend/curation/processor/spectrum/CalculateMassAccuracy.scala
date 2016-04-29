package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.common.{CurationUtilities, CommonMetaData, CommonTags}
import edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical.AdductBuilder
import org.springframework.batch.item.ItemProcessor

import scala.reflect.ClassTag

/**
  * Created by sajjan on 3/22/16.
  */
@Step(description = "this step will calculate the mass accuracy of the given spectrum")
class CalculateMassAccuracy extends ItemProcessor[Spectrum,Spectrum] with LazyLogging {

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    // Get computed total exact mass from the biological compound if it exists
    val biologicalCompound: Compound = CurationUtilities.getFirstBiologicalCompound(spectrum)

    if (biologicalCompound == null) {
      logger.info(s"Biological compound not found in spectrum ${spectrum.id}")
      spectrum
    }

    else {
      val theoreticalMass: String = findMetaDataValue(biologicalCompound.metaData, CommonMetaData.TOTAL_EXACT_MASS)

      // Get precursor mass and type from the spectrum if it exists
      val precursorMass: String = findMetaDataValue(spectrum.metaData, CommonMetaData.PRECURSOR_MASS)
      val precursorType: String = findMetaDataValue(spectrum.metaData, CommonMetaData.PRECURSOR_MASS)


      if (theoreticalMass == null) {
        logger.info(s"Computed exact mass was not found in spectrum ${spectrum.id}")
        spectrum
      }

      else if (precursorMass == null) {
        logger.info(s"Precursor mass was not found in spectrum ${spectrum.id}")
        spectrum
      }

      else if (precursorType == null) {
        logger.info(s"Precursor type was not found in spectrum ${spectrum.id}")
        spectrum
      }

      else if (!AdductBuilder.LCMS_POSITIVE_ADDUCTS.contains(precursorType) &&
        !AdductBuilder.LCMS_NEGATIVE_ADDUCTS.contains(precursorType)) {

        logger.info(s"Precursor type ${precursorType} is not a valid adduct type in spectrum ${spectrum.id}")
        spectrum
      }

      else {
        val computedMass: Double = AdductBuilder.LCMS_POSITIVE_ADDUCTS.contains(precursorType) match {
          case true => AdductBuilder.LCMS_POSITIVE_ADDUCTS(precursorType)(precursorMass.asInstanceOf[Double])
          case false => AdductBuilder.LCMS_NEGATIVE_ADDUCTS(precursorType)(precursorMass.asInstanceOf[Double])
        }

        val massError: Double = precursorMass.asInstanceOf[Double] - computedMass
        val massAccuracy: Double = Math.abs(massError) / precursorMass.asInstanceOf[Double] * 1000000

        logger.info(s"Calculated mass accuracy ${massAccuracy} for spectrum ${spectrum.id}")

        val updatedMetaData: Array[MetaData] = spectrum.metaData :+
          new MetaData("mass spectrometry", true, false, CommonMetaData.MASS_ACCURACY, null, "ppm", null, massAccuracy) :+
          new MetaData("mass spectrometry", true, false, CommonMetaData.MASS_ERROR, null, "mDa", null, 1000 * massAccuracy)

        spectrum.copy(
          metaData = updatedMetaData
        )
      }
    }
  }

  /**
    * Find the string value for a given metadata name, or null if not found
    *
    * @param metaData
    * @param name
    * @return
    */
  def findMetaDataValue(metaData: Array[MetaData], name: String): String = {
    metaData.filter(_.name == name) match {
      case x: Array[MetaData] if x.nonEmpty => x.head.asInstanceOf[String]
      case _ => null
    }
  }
}