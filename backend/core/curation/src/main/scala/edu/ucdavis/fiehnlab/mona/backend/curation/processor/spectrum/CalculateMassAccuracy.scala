package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{CompoundDAO, MetaDataDAO, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical.AdductBuilder
import org.springframework.batch.item.ItemProcessor
import scala.collection.mutable.{Buffer, ArrayBuffer}
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 3/22/16.
  */
@Step(description = "this step will calculate the mass accuracy of the given spectrum")
class CalculateMassAccuracy extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    // Get computed total exact mass from the biological compound if it exists
    val biologicalCompound: CompoundDAO=
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
    }

    else {
      val theoreticalMass: String = CurationUtilities.findMetaDataValue(biologicalCompound.getMetaData.asScala, CommonMetaData.TOTAL_EXACT_MASS)

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


      if (theoreticalMass == null) {
        logger.info(s"${spectrum.getId}: Computed exact mass was not found, unable to calculate mass accuracy")
        spectrum
      }

      else if (precursorMass < 0) {
        logger.info(s"${spectrum.getId}: Precursor mass was not found, unable to calculate mass accuracy")
        spectrum
      }

      else if (precursorType == null) {
        logger.info(s"${spectrum.getId}: Precursor type was not found, unable to calculate mass accuracy")
        spectrum
      }

      else if (adductFunction == null) {
        logger.info(s"${spectrum.getId}: Precursor type $precursorType is an unrecognized adduct type, unable to calculate mass accuracy")
        spectrum
      }

      else {
        val computedMass: Double = adductFunction(theoreticalMass.toDouble)

        val massError: Double = precursorMass - computedMass
        val massAccuracy: Double = Math.abs(massError) / precursorMass * 1000000

        logger.info(s"${spectrum.getId}: Calculated mass accuracy $massAccuracy and mass error $massError Da")

        val updatedMetaData: Buffer[MetaDataDAO] = spectrum.getMetaData.asScala :+
          new MetaDataDAO(null, CommonMetaData.MASS_ACCURACY, massAccuracy.toString, false, "mass spectrometry", true, "ppm") :+
          new MetaDataDAO(null, CommonMetaData.MASS_ERROR, massError.toString, false, "mass spectrometry", true, "Da")

        spectrum.setMetaData(updatedMetaData.asJava)
        spectrum
      }
    }
  }
}
