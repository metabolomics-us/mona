package edu.ucdavis.fiehnlab.mona.backend.curation.processor.validation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{MetaDataDAO, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import org.springframework.batch.item.ItemProcessor
import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 4/21/16.
  */
@Step(description = "this step will validate the computed mass accuracy information")
class MassAccuracyValidation extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  val MINIMUM_ACCURACY: Double = 250.0
  val GOOD_ACCURACY: Double = 5.0
  val HIGH_ACCURACY: Double = 1.0

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    val massAccuracyMetaData: Buffer[MetaDataDAO] = spectrum.getMetaData.asScala.filter(x => x.getName == CommonMetaData.MASS_ACCURACY && x.getComputed)

    if (massAccuracyMetaData.isEmpty) {
      logger.debug(s"${spectrum.getId}: Mass accuracy not defined for specturm")
      spectrum
    }

    else {
      logger.info(s"Mass accuracy value is: ${massAccuracyMetaData.head.getValue}")
      val massAccuracy: Double = massAccuracyMetaData.head.getValue.toDouble

      if (massAccuracy <= HIGH_ACCURACY) {
        logger.info(s"${spectrum.getId}: Has a high mass accuracy of $massAccuracy")

        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, 2, s"High mass accuracy of ${"%.3f".format(massAccuracy)} ppm"))
        spectrum
      } else if (massAccuracy <= GOOD_ACCURACY) {
        logger.info(s"${spectrum.getId}: Has a good mass accuracy of $massAccuracy")

        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, 1, s"Mass accuracy of ${"%.3f".format(massAccuracy)} ppm"))
        spectrum

      } else if (massAccuracy > MINIMUM_ACCURACY) {
        logger.info(s"S${spectrum.getId}: Has a poor mass accuracy of $massAccuracy, greater than the threshold of $MINIMUM_ACCURACY")

        spectrum.setScore(
          CurationUtilities.addImpact(spectrum.getScore, -1, s"Poor mass accuracy of ${"%.3f".format(massAccuracy)} ppm")
        )
        spectrum
      } else {
        logger.info(s"${spectrum.getId}: Has mass accuracy of $massAccuracy, within the threshold of $MINIMUM_ACCURACY")
        spectrum
      }
    }
  }
}
