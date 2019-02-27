package edu.ucdavis.fiehnlab.mona.backend.curation.processor.validation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import org.springframework.batch.item.ItemProcessor

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
    val massAccuracyMetaData: Array[MetaData] = spectrum.metaData.filter(x => x.name == CommonMetaData.MASS_ACCURACY && x.computed)

    if (massAccuracyMetaData.isEmpty) {
      logger.debug(s"${spectrum.id}: Mass accuracy not defined for specturm")
      spectrum
    }

    else {
      logger.info(massAccuracyMetaData.head.toString)
      val massAccuracy: Double = massAccuracyMetaData.head.value.asInstanceOf[Double]

      if (massAccuracy <= HIGH_ACCURACY) {
        logger.info(s"${spectrum.id}: Has a high mass accuracy of $massAccuracy")

        spectrum.copy(
          score = CurationUtilities.addImpact(spectrum.score, 2, s"High mass accuracy of ${"%.3f".format(massAccuracy)} ppm")
        )
      } else if (massAccuracy <= GOOD_ACCURACY) {
        logger.info(s"${spectrum.id}: Has a good mass accuracy of $massAccuracy")

        spectrum.copy(
          score = CurationUtilities.addImpact(spectrum.score, 1, s"Mass accuracy of ${"%.3f".format(massAccuracy)} ppm")
        )
      } else if (massAccuracy > MINIMUM_ACCURACY) {
        logger.info(s"S${spectrum.id}: Has a poor mass accuracy of $massAccuracy, greater than the threshold of $MINIMUM_ACCURACY")

        spectrum.copy(
          score = CurationUtilities.addImpact(spectrum.score, -1, s"Poor mass accuracy of ${"%.3f".format(massAccuracy)} ppm")
        )
      } else {
        logger.info(s"${spectrum.id}: Has mass accuracy of $massAccuracy, within the threshold of $MINIMUM_ACCURACY")
        spectrum
      }
    }
  }
}