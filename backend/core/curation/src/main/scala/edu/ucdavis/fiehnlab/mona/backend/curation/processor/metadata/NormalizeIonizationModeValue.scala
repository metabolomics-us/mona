package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
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
    spectrum.copy(
      metaData = normalizeIonModeData(spectrum.metaData, spectrum.id)
    )
  }

  /**
    * rename metadata names according to a provided synonym list
    *
    * @param metaData
    * @return
    */
  def normalizeIonModeData(metaData: Array[MetaData], id: String): Array[MetaData] = {

    val matches: Array[MetaData] = metaData.filter(_.name == CommonMetaData.IONIZATION_MODE)

    // Look at existing ionization mode values
    if (matches.nonEmpty) {
      // If data is normalized, we're done
      if (matches.exists(x => x.value == "positive" || x.value == "negative")) {
        metaData
      }

      // Otherwise, if we have one match, normalize the data
      else if (matches.length == 1) {
        val updatedMetaData: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()
        updatedMetaData ++= metaData.filter(_.name != CommonMetaData.IONIZATION_MODE)

        val value: String = matches.head.value.toString.toLowerCase.trim

        if(POSITIVE_TERMS.contains(value)) {
          logger.info(s"$id: Identified ionization type 'value' as positive mode")
          updatedMetaData.append(matches.head.copy(value = "positive"))
        }

        else if(NEGATIVE_TERMS.contains(value)) {
          logger.info(s"$id: Identified ionization type 'value' as negative mode")
          updatedMetaData.append(matches.head.copy(value = "negative"))
        }

        else {
          logger.warn(s"$id: Ionization type value 'value' was unidentifiable - keeping metadata value")
          updatedMetaData.append(matches.head)
        }

        updatedMetaData.toArray
      } else {
        matches.foreach { x => logger.warn(s"\t${x.name} = ${x.value}")}
        logger.warn(s"$id: Multiple ionization mode matches!")
        metaData
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
          logger.info(s"$id: Possible positive mode metadata value found: ${x.name} = ${x.value}")

          foundPositive += 1
          possibleIonModeData.append(x.copy(name = CommonMetaData.IONIZATION_MODE, value = "positive"))
        }

        else if (NEGATIVE_TERMS.contains(value)) {
          logger.info(s"$id: Possible negative mode metadata value found: ${x.name} = ${x.value}")

          foundNegative += 1
          possibleIonModeData.append(x.copy(name = CommonMetaData.IONIZATION_MODE, value = "negative"))
        }

        else {
          updatedMetaData.append(x)
        }
      }

      if (foundPositive + foundNegative == 0) {
        logger.info(s"$id: Unable to identify ionization mode")
        metaData
      } else if (foundPositive + foundNegative == 1) {
        logger.info(s"$id: Identified ionization mode as "+ (if (foundPositive > 0) "positive" else "negative"))
        updatedMetaData.append(possibleIonModeData.head)
      } else {
        logger.warn(s"$id: Multiple ionization mode matches!")
        updatedMetaData ++= possibleIonModeData
      }

      updatedMetaData.toArray
    }
  }
}