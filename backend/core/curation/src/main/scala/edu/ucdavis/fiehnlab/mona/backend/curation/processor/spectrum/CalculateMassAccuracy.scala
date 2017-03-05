package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical.AdductBuilder
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 3/22/16.
  */
@Step(description = "this step will calculate the mass accuracy of the given spectrum")
class CalculateMassAccuracy extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

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
      logger.info(s"${spectrum.id}: Biological compound not found in spectrum ${spectrum.id}")
      spectrum
    }

    else {
      val theoreticalMass: String = findMetaDataValue(biologicalCompound.metaData, CommonMetaData.TOTAL_EXACT_MASS)

      // Get precursor mass and type from the spectrum if it exists
      val precursorMass: String = findMetaDataValue(spectrum.metaData, CommonMetaData.PRECURSOR_MASS)
      val precursorType: String = findMetaDataValue(spectrum.metaData, CommonMetaData.PRECURSOR_TYPE)

      if (theoreticalMass == null) {
        logger.info(s"${spectrum.id}: Computed exact mass was not found in spectrum ${spectrum.id}")
        spectrum
      }

      else if (precursorMass == null) {
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

        val massError: Double = precursorMass.toDouble - computedMass
        val massAccuracy: Double = Math.abs(massError) / precursorMass.toDouble * 1000000

        logger.info(s"${spectrum.id}: Calculated mass accuracy $massAccuracy and mass error ${massError * 1000} mDa")

        val updatedMetaData: Array[MetaData] = spectrum.metaData :+
          MetaData("mass spectrometry", computed = true, hidden = false, CommonMetaData.MASS_ACCURACY, null, "ppm", null, massAccuracy) :+
          MetaData("mass spectrometry", computed = true, hidden = false, CommonMetaData.MASS_ERROR, null, "mDa", null, 1000 * massAccuracy)

        spectrum.copy(metaData = updatedMetaData)
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
      case x: Array[MetaData] if x.nonEmpty => x.head.value.toString
      case _ => null
    }
  }
}