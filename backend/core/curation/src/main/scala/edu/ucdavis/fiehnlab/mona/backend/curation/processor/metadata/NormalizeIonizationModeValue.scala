package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.{ArrayBuffer, Buffer}
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 4/16/16.
  */
@Step(description = "this step will update ionization type metadata values to standard values")
class NormalizeIonizationModeValue extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  val POSITIVE_TERMS: Array[String] = Array("positive", "pos", "p", "+")
  val NEGATIVE_TERMS: Array[String] = Array("negative", "neg", "n", "-")
  val ALL_TERMS: Array[String] = POSITIVE_TERMS ++ NEGATIVE_TERMS

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {

    val metaData: Buffer[MetaData] = spectrum.getMetaData.asScala
    val matches: Buffer[MetaData] = metaData.filter(_.getName.toLowerCase == CommonMetaData.IONIZATION_MODE.toLowerCase)

    // Look at existing ionization mode values
    if (matches.nonEmpty) {
      // If data is normalized, we're done
      if (matches.exists(x => x.getValue == "positive" || x.getValue == "negative")) {
        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, 1, "Ionization mode/type provided"))
        spectrum
      }

      // Otherwise, if we have one match, normalize the data
      else if (matches.length == 1) {
        val updatedMetaData: Buffer[MetaData] = metaData.filter(_.getName != CommonMetaData.IONIZATION_MODE)

        val value: String = matches.head.getValue.toString.toLowerCase.trim

        if (POSITIVE_TERMS.contains(value)) {
          logger.info(s"${spectrum.getId}: Identified ionization type 'value' as positive mode")

          matches.head.setValue("positive")
          spectrum.setMetaData((updatedMetaData :+ matches.head).asJava)
          spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, 1, "Ionization mode/type provided"))
          spectrum
        }

        else if (NEGATIVE_TERMS.contains(value)) {
          logger.info(s"${spectrum.getId}: Identified ionization type 'value' as negative mode")

          matches.head.setValue("negative")
          spectrum.setMetaData((updatedMetaData :+ matches.head).asJava)
          spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, 1, "Ionization mode/type provided"))
          spectrum
        }

        else {
          logger.warn(s"${spectrum.getId}: Ionization type value 'value' was unidentifiable - keeping metadata value")

          spectrum.setMetaData((updatedMetaData :+ matches.head).asJava)
          spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, -1, "Ionization mode/type unidentifiable"))
          spectrum
        }
      } else {
        matches.foreach { x => logger.warn(s"\t${x.getName} = ${x.getValue}") }
        logger.warn(s"${spectrum.getId}: Multiple ionization mode matches!")
        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, -1, "Multiple ionization mode/types identified"))
        spectrum
      }
    }

    // Update incorrectly named ionization mode values
    else {
      val updatedMetaData: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()
      val possibleIonModeData: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()

      var foundPositive = 0
      var foundNegative = 0

      metaData.foreach { x =>
        val value: String = x.getValue.toString.toLowerCase.trim

        if (POSITIVE_TERMS.contains(value)) {
          logger.info(s"${spectrum.getId}: Possible positive mode metadata value found: ${x.getName} = ${x.getValue}")

          foundPositive += 1
          x.setName(CommonMetaData.IONIZATION_MODE)
          x.setValue("positive")
          possibleIonModeData.append(x)
        }

        else if (NEGATIVE_TERMS.contains(value)) {
          logger.info(s"${spectrum.getId}: Possible negative mode metadata value found: ${x.getName} = ${x.getValue}")

          foundNegative += 1
          x.setName(CommonMetaData.IONIZATION_MODE)
          x.setValue("negative")
          possibleIonModeData.append(x)
        }

        else {
          updatedMetaData.append(x)
        }
      }

      if (foundPositive + foundNegative == 0) {
        logger.info(s"${spectrum.getId}: Unable to identify ionization mode")

        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, -1, "Ionization mode/type unidentifiable"))
        spectrum
      } else if (foundPositive + foundNegative == 1) {
        logger.info(s"${spectrum.getId}: Identified ionization mode as " + (if (foundPositive > 0) "positive" else "negative"))

        spectrum.setMetaData((updatedMetaData :+ possibleIonModeData.head).asJava)
        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, 1, "Ionization mode/type provided"))
        spectrum
      } else {
        logger.warn(s"${spectrum.getId}: Multiple ionization mode matches!")

        spectrum.setMetaData((updatedMetaData ++ possibleIonModeData).asJava)
        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, -1, "Multiple ionization mode/types identified"))
        spectrum
      }
    }
  }
}
