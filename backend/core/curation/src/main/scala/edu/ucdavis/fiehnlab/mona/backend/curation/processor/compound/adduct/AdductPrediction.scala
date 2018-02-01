package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.adduct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical.AdductBuilder
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 1/19/18.
  */
@Step(description = "this step will validate the given adduct information, predicting values if possible")
class AdductPrediction extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  private val PRECURSOR_MATCH_TOLERANCE: Double = 0.1

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
    } else {
      // Get total exact mass from biological compound
      val theoreticalMass: Double = {
        val x: String = CurationUtilities.findMetaDataValue(biologicalCompound.metaData, CommonMetaData.TOTAL_EXACT_MASS)

        if (x == null) {
          -1
        } else {
          x.toDouble
        }
      }

      // Get ionization mode
      val ionizationMode: String = CurationUtilities.findMetaDataValue(spectrum.metaData, CommonMetaData.IONIZATION_MODE)

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

      // Find a matching adduct
      val (adductMatch, adductMode, adductFunction): (String, String, Double => Double) = AdductBuilder.findAdduct(precursorType)


      if (theoreticalMass < 0) {
        logger.info(s"${spectrum.id}: Computed exact mass was not found, unable to validate adduct/precursor information")
        spectrum
      }

      else if (precursorMass < 0 && adductFunction == null) {
        logger.info(s"${spectrum.id}: Precursor m/z and type were not found, unable to validate adduct/precursor information")
        spectrum
      }

      // Predict a precursor mass
      else if (precursorMass < 0 && adductFunction != null) {
        val predictedPrecursorMass = adductFunction(theoreticalMass)
        logger.info(s"${spectrum.id}: Predicting precursor m/z = $predictedPrecursorMass given theoretical mass = $theoreticalMass and adduct = $adductMatch")

        spectrum.copy(
          metaData = spectrum.metaData :+ MetaData("mass spectrometry", computed = true, hidden = false, CommonMetaData.PRECURSOR_MASS, null, null, null, predictedPrecursorMass)
        )
      }

      // Predict a precursor type
      else if (precursorMass > 0 && adductFunction == null) {
        val adductMap =
          if (ionizationMode == "positive") {
            AdductBuilder.LCMS_POSITIVE_ADDUCTS
          } else if (ionizationMode == "negative") {
            AdductBuilder.LCMS_NEGATIVE_ADDUCTS
          } else {
            AdductBuilder.LCMS_POSITIVE_ADDUCTS ++ AdductBuilder.LCMS_NEGATIVE_ADDUCTS
          }

        // Find the nearest matching adduct to the given precursor mass
        val (dist, predictedAdduct): (Double, String) = adductMap.map { case (x, f) => (Math.abs(f(theoreticalMass) - precursorMass), x) }.minBy(_._1)

        if (dist < PRECURSOR_MATCH_TOLERANCE) {
          logger.info(s"${spectrum.id}: Predicting precursor type = $predictedAdduct given theoretical mass = $theoreticalMass, precursor m/z = $precursorMass and delta = $dist")

          spectrum.copy(
            metaData = spectrum.metaData :+ MetaData("mass spectrometry", computed = true, hidden = false, CommonMetaData.PRECURSOR_TYPE, null, null, null, predictedAdduct)
          )
        } else {
          logger.info(s"${spectrum.id}: Unable to determine precursor type given theoretical mass = $theoreticalMass, precursor m/z = $precursorMass and delta = $dist")

          spectrum.copy(
            score = CurationUtilities.addImpact(spectrum.score, -1.0, "Unable to determine a valid adduct for the provided compound and precursor m/z")
          )
        }
      }

      else {
        logger.info(s"${spectrum.id}: Precursor information validated successfully")

        spectrum.copy(
          score = CurationUtilities.addImpact(spectrum.score, 1.0, "Precursor information and provided compound validated")
        )
      }
    }
  }
}
