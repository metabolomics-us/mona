package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.ArrayBuffer

/**
  * Created by sajjan on 4/16/16.
  */
@Step(description = "this step will update ionization type metadata values to standard values")
class NormalizeIonizationModeValue extends ItemProcessor[Spectrum,Spectrum] with LazyLogging {
  val POSITIVE_TERMS: Array[String] = Array("positive", "pos", "p", "+")
  val NEGATIVE_TERMS: Array[String] = Array("negative", "neg", "n", "-")
  val ALL_TERMS: Array[String] = POSITIVE_TERMS ++ NEGATIVE_TERMS

  /**
    * processes the given spectrum and removes all it's computed meta data
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {

    val metaData: Array[MetaData] = spectrum.metaData
    val matches: Array[MetaData] = metaData.filter(_.name == CommonMetaData.IONIZATION_MODE)

    // Look at existing ionization mode values
    if (matches.nonEmpty) {
      // If data is normalized, we're done
      if (matches.exists(x => x.value == "positive" || x.value == "negative")) {
        spectrum.copy(score = CurationUtilities.addImpact(spectrum.score, 1, "Ionization mode/type provided"))
      }

      // Otherwise, if we have one match, normalize the data
      else if (matches.length == 1) {
        val updatedMetaData: Array[MetaData] = metaData.filter(_.name != CommonMetaData.IONIZATION_MODE)

        val value: String = matches.head.value.toString.toLowerCase.trim

        if(POSITIVE_TERMS.contains(value)) {
          logger.info(s"${spectrum.id}: Identified ionization type 'value' as positive mode")

          spectrum.copy(
            metaData = updatedMetaData :+ matches.head.copy(value = "positive"),
            score = CurationUtilities.addImpact(spectrum.score, 1, "Ionization mode/type provided")
          )
        }

        else if(NEGATIVE_TERMS.contains(value)) {
          logger.info(s"${spectrum.id}: Identified ionization type 'value' as negative mode")

          spectrum.copy(
            metaData = updatedMetaData :+ matches.head.copy(value = "negative"),
            score = CurationUtilities.addImpact(spectrum.score, 1, "Ionization mode/type provided")
          )
        }

        else {
          logger.warn(s"${spectrum.id}: Ionization type value 'value' was unidentifiable - keeping metadata value")

          spectrum.copy(
            metaData = updatedMetaData :+ matches.head,
            score = CurationUtilities.addImpact(spectrum.score, -1, "Ionization mode/type unidentifiable")
          )
        }
      } else {
        matches.foreach { x => logger.warn(s"\t${x.name} = ${x.value}")}
        logger.warn(s"${spectrum.id}: Multiple ionization mode matches!")

        spectrum.copy(score = CurationUtilities.addImpact(spectrum.score, -1, "Multiple ionization mode/types identified"))
      }
    }

    // Update incorrectly named ionization mode values
    else {
      val updatedMetaData: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()
      val possibleIonModeData: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()

      var foundPositive = 0
      var foundNegative = 0

      metaData.foreach { x =>
        val value: String = x.value.toString.toLowerCase.trim

        if (POSITIVE_TERMS.contains(value)) {
          logger.info(s"${spectrum.id}: Possible positive mode metadata value found: ${x.name} = ${x.value}")

          foundPositive += 1
          possibleIonModeData.append(x.copy(name = CommonMetaData.IONIZATION_MODE, value = "positive"))
        }

        else if (NEGATIVE_TERMS.contains(value)) {
          logger.info(s"${spectrum.id}: Possible negative mode metadata value found: ${x.name} = ${x.value}")

          foundNegative += 1
          possibleIonModeData.append(x.copy(name = CommonMetaData.IONIZATION_MODE, value = "negative"))
        }

        else {
          updatedMetaData.append(x)
        }
      }

      if (foundPositive + foundNegative == 0) {
        logger.info(s"${spectrum.id}: Unable to identify ionization mode")

        spectrum.copy(score = CurationUtilities.addImpact(spectrum.score, -1, "Ionization mode/type unidentifiable"))
      } else if (foundPositive + foundNegative == 1) {
        logger.info(s"${spectrum.id}: Identified ionization mode as "+ (if (foundPositive > 0) "positive" else "negative"))

        spectrum.copy(
          metaData = (updatedMetaData :+ possibleIonModeData.head).toArray,
          score = CurationUtilities.addImpact(spectrum.score, 1, "Ionization mode/type provided")
        )
      } else {
        logger.warn(s"${spectrum.id}: Multiple ionization mode matches!")

        spectrum.copy(
          metaData = (updatedMetaData ++ possibleIonModeData).toArray,
          score = CurationUtilities.addImpact(spectrum.score, -1, "Multiple ionization mode/types identified")
        )
      }
    }
  }
}