package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.adduct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical.AdductBuilder
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import org.springframework.batch.item.ItemProcessor
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 1/19/18.
  */
@Step(description = "this step will validate the given adduct information, predicting values if possible")
class AdductPrediction extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  private val PRECURSOR_MATCH_TOLERANCE: Double = 0.5

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    // Get computed total exact mass from the biological compound if it exists
    val biologicalCompound: Compound =
      if (spectrum.getCompound.asScala.exists(_.getKind == "biological")) {
        spectrum.getCompound.asScala.find(_.getKind == "biological").head
      } else if (spectrum.getCompound.asScala.nonEmpty) {
        spectrum.getCompound.asScala.head
      } else {
        null
      }

    if (biologicalCompound == null) {
      logger.info(s"${spectrum.getId}: Valid compound could not be found!")
      spectrum
    } else {
      // Get total exact mass from biological compound
      val theoreticalMass: Double = {
        val x: String = CurationUtilities.findMetaDataValue(biologicalCompound.getMetaData.asScala, CommonMetaData.TOTAL_EXACT_MASS)

        if (x == null) {
          -1
        } else {
          x.toDouble
        }
      }

      // Get ionization mode
      val ionizationMode: String = CurationUtilities.findMetaDataValue(spectrum.getMetaData.asScala, CommonMetaData.IONIZATION_MODE)

      // Get precursor mass and type from the spectrum if it exists
      val precursorMass: Double = {
        val x: String = CurationUtilities.findMetaDataValue(spectrum.getMetaData.asScala, CommonMetaData.PRECURSOR_MASS)

        if (x == null) {
          -1
        } else {
          try {
            x.split('/').last.toDouble
          } catch {
            case e: Throwable =>
              logger.warn(s"${spectrum.getId}: Invalid precursor m/z: '$x'")
              -1
          }
        }
      }

      // Handle the case where multiple precursor types are given separated by slashes
      val precursorType: String = {
        val x: String = CurationUtilities.findMetaDataValue(spectrum.getMetaData.asScala, CommonMetaData.PRECURSOR_TYPE)

        if (x == null) {
          x
        } else {
          x.split('/').last
        }
      }

      // Find a matching adduct
      val (adductMatch, adductMode, adductFunction): (String, String, Double => Double) = AdductBuilder.findAdduct(precursorType)


      if (theoreticalMass < 0) {
        logger.info(s"${spectrum.getId}: Computed exact mass was not found, unable to validate adduct/precursor information")
        for (meta <- spectrum.getCompound.asScala.head.getMetaData.asScala) {
          logger.info(s"MetaName: ${meta.getName} and computed: ${meta.getComputed}")
        }
        spectrum
      }

      else if (precursorMass < 0 && adductFunction == null) {
        logger.info(s"${spectrum.getId}: Precursor m/z and type were not found, unable to validate adduct/precursor information")
        for (meta <- spectrum.getCompound.asScala.head.getMetaData.asScala) {
          logger.info(s"MetaName: ${meta.getName} and computed: ${meta.getComputed}")
        }
        spectrum
      }

      // Predict a precursor mass
      else if (precursorMass < 0 && adductFunction != null) {
        val predictedPrecursorMass = adductFunction(theoreticalMass)
        logger.info(s"${spectrum.getId}: Predicting precursor m/z = $predictedPrecursorMass given theoretical mass = $theoreticalMass and adduct = $adductMatch")

        val addedList = spectrum.getMetaData
        addedList.add(new MetaData(null, CommonMetaData.PRECURSOR_MASS, predictedPrecursorMass.toString, false, "mass spectrometry", true, null))
        spectrum.setMetaData(addedList)
        for (meta <- spectrum.getCompound.asScala.head.getMetaData.asScala) {
          logger.info(s"MetaName: ${meta.getName} and computed: ${meta.getComputed}")
        }
        spectrum
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
          logger.info(s"${spectrum.getId}: Predicting precursor type = $predictedAdduct given theoretical mass = $theoreticalMass, precursor m/z = $precursorMass and delta = $dist")

          val addedList = spectrum.getMetaData
          addedList.add(new MetaData(null, CommonMetaData.PRECURSOR_TYPE, predictedAdduct, false, "mass spectrometry", true, null))
          spectrum.setMetaData(addedList)
          spectrum
        } else {
          logger.info(s"${spectrum.getId}: Unable to determine precursor type given theoretical mass = $theoreticalMass, precursor m/z = $precursorMass and delta = $dist")

          val score = CurationUtilities.addImpact(spectrum.getScore, -5, "Unable to determine a valid adduct for the provided compound and precursor m/z")
          spectrum.setScore(score)
          for (meta <- spectrum.getCompound.asScala.head.getMetaData.asScala) {
            logger.info(s"MetaName: ${meta.getName} and computed: ${meta.getComputed}")
          }
          spectrum
        }
      }

      else {
        logger.info(s"${spectrum.getId}: Precursor information validated successfully")

        val score = CurationUtilities.addImpact(spectrum.getScore, 1, "Precursor information and provided compound validated")
        spectrum.setScore(score)
        for (meta <- spectrum.getCompound.asScala.head.getMetaData.asScala) {
          logger.info(s"MetaName: ${meta.getName} and computed: ${meta.getComputed}")
        }
        spectrum
      }
    }
  }
}
