package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical.AdductBuilder
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 3/22/16.
  */
@Step(description = "this step will calculate the mass accuracy of the given spectrum")
class CalculateAdductAndMassAccuracy extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  /**
    * Processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    // Get computed total exact mass from the biological compound if it exists
    val biologicalCompound: Compound =
      if (spectrum.compound.exists(_.kind == "biological")) {
        spectrum.compound.find(_.kind == "biological").head
      } else if (spectrum.compound.nonEmpty) {
        spectrum.compound.head
      } else {
        null
      }

    if (biologicalCompound == null) {
      logger.info(s"${spectrum.id}: Valid compound could not be found!")
      spectrum
    }

    else {
      val theoreticalMass: String = CurationUtilities.findMetaDataValue(biologicalCompound.metaData, CommonMetaData.TOTAL_EXACT_MASS)

      // Get precursor mass and type from the spectrum if it exists
      val precursorMass: Double = {
        val x: String = CurationUtilities.findMetaDataValue(spectrum.metaData, CommonMetaData.PRECURSOR_MASS)

        if (x == null) {
          -1
        } else {
          try {
            x.split('/').last.toDouble
          } catch {
            case e: Throwable =>
              logger.warn(s"${spectrum.id}: Invalid precursor m/z: '$x'")
              -1
          }
        }
      }

      // Handle the case where multiple precursor types are given separated by slashes
      val precursorType: String = {
        val x: String = CurationUtilities.findMetaDataValue(spectrum.metaData, CommonMetaData.PRECURSOR_TYPE)

        if (x == null) {
          x
        } else {
          x.split('/').last
        }
      }

      if (theoreticalMass == null) {
        logger.info(s"${spectrum.id}: Computed exact mass was not found in spectrum")
        spectrum
      }

      else if (precursorMass < 0 && precursorType == null) {
        logger.info(s"${spectrum.id}: No precursor information found, unable to proceed")
        spectrum
      }





      else if (precursorMass < 0) {
        logger.info(s"${spectrum.id}: Precursor mass was not found in spectrum ${spectrum.id}")
        spectrum
      }

      else if (precursorType == null) {
        logger.info(s"${spectrum.id}: Precursor type was not found in spectrum ${spectrum.id}")
        spectrum
      }

      else if (!AdductBuilder.LCMS_POSITIVE_ADDUCTS.contains(precursorType) &&
        !AdductBuilder.LCMS_NEGATIVE_ADDUCTS.contains(precursorType)) {

        logger.info(s"${spectrum.id}: Precursor type $precursorType is not a valid adduct type in spectrum ${spectrum.id}")
        spectrum
      }

      else {
        val computedMass: Double =
          if (AdductBuilder.LCMS_POSITIVE_ADDUCTS.contains(precursorType)) {
            AdductBuilder.LCMS_POSITIVE_ADDUCTS(precursorType)(theoreticalMass.toDouble)
          } else {
            AdductBuilder.LCMS_NEGATIVE_ADDUCTS(precursorType)(theoreticalMass.toDouble)
          }

        val massError: Double = precursorMass - computedMass
        val massAccuracy: Double = Math.abs(massError) / precursorMass * 1000000

        logger.info(s"${spectrum.id}: Calculated mass accuracy $massAccuracy and mass error $massError Da")

        val updatedMetaData: Array[MetaData] = spectrum.metaData :+
          MetaData("mass spectrometry", computed = true, hidden = false, CommonMetaData.MASS_ACCURACY, null, "ppm", null, massAccuracy) :+
          MetaData("mass spectrometry", computed = true, hidden = false, CommonMetaData.MASS_ERROR, null, "Da", null, massError)

        spectrum.copy(metaData = updatedMetaData)
      }
    }
  }


  private def calculateAdduct(spectrum: Spectrum, theoreticalMass: Double, precursorMass: Double, precursorType: String): Spectrum = {

    // Predict adduct
    if (precursorType == null || (!AdductBuilder.LCMS_POSITIVE_ADDUCTS.contains(precursorType) && !AdductBuilder.LCMS_NEGATIVE_ADDUCTS.contains(precursorType))) {
      if (precursorType != null) {
        logger.info(s"${spectrum.id}: Precursor type $precursorType is not a recognized, attempting to predict one")
      }


    }

  }


  /**
    * Calculates and adds mass accuracy metadata fields if able
    * @param spectrum
    * @param theoreticalMass
    * @param precursorMass
    * @param precursorType
    * @return
    */
  private def calculateMassAccuracy(spectrum: Spectrum, theoreticalMass: Double, precursorMass: Double, precursorType: String): Spectrum = {
    if (precursorMass < 0) {
      logger.info(s"${spectrum.id}: Precursor mass was not found, unable to calculate mass accuracy")
      spectrum
    } else if (precursorType == null) {
      logger.info(s"${spectrum.id}: Precursor type was not found, unable to calculate mass accuracy")
      spectrum
    } else if (!AdductBuilder.LCMS_POSITIVE_ADDUCTS.contains(precursorType) && !AdductBuilder.LCMS_NEGATIVE_ADDUCTS.contains(precursorType)) {
      logger.info(s"${spectrum.id}: Precursor type $precursorType is an unrecognized adduct type, unable to calculate mass accuracy")
      spectrum
    } else {
      val computedMass: Double =
        if (AdductBuilder.LCMS_POSITIVE_ADDUCTS.contains(precursorType)) {
          AdductBuilder.LCMS_POSITIVE_ADDUCTS(precursorType)(theoreticalMass.toDouble)
        } else {
          AdductBuilder.LCMS_NEGATIVE_ADDUCTS(precursorType)(theoreticalMass.toDouble)
        }

      val massError: Double = precursorMass - computedMass
      val massAccuracy: Double = Math.abs(massError) / precursorMass * 1000000

      logger.info(s"${spectrum.id}: Calculated mass accuracy $massAccuracy and mass error $massError Da")

      val updatedMetaData: Array[MetaData] = spectrum.metaData :+
        MetaData("mass spectrometry", computed = true, hidden = false, CommonMetaData.MASS_ACCURACY, null, "ppm", null, massAccuracy) :+
        MetaData("mass spectrometry", computed = true, hidden = false, CommonMetaData.MASS_ERROR, null, "Da", null, massError)

      spectrum.copy(metaData = updatedMetaData)
    }
  }
}