package edu.ucdavis.fiehnlab.mona.backend.curation.processor.validation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.common.CommonMetaData
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 4/21/16.
  */
@Step(description = "this step will validate the computed mass accuracy information")
class MassAccuracyValidation extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {
  val MINIMUM_ACCURACY: Double = 5.0

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    val massAccuracyMetaData: Array[MetaData] = spectrum.metaData.filter(_.name == CommonMetaData.MASS_ACCURACY)

    if (massAccuracyMetaData.isEmpty) {
      logger.trace(s"Mass accuracy not defined for specturm ${spectrum.id}")
    }

    else {
      val massAccuracy: Double = massAccuracyMetaData.head.value.asInstanceOf[Double]

      if (massAccuracy > MINIMUM_ACCURACY) {
        logger.warn(s"Spectrum ${spectrum.id} has mass accuracy of $massAccuracy, greater than the threshold of $MINIMUM_ACCURACY")
      } else {
        logger.info(s"Spectrum ${spectrum.id} has mass accuracy of $massAccuracy, within the threshold of $MINIMUM_ACCURACY")
      }
    }

    spectrum
  }
}