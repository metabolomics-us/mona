package edu.ucdavis.fiehnlab.mona.backend.curation.processor.library

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 4/16/16.
  */
@Step(description = "this step will process a MassBank spectrum")
class MassBankProcessor extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {
  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {


    spectrum
  }
}