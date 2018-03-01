package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CurationUtilities
import org.springframework.batch.item.ItemProcessor

@Step(description = "this step will score the spectrum based on the number of ions present")
class SpectrumIonCountScoringRule  extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  /**
    * processes the given spectrum and removes all it's computed meta data
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {

    if (spectrum.spectrum.split(" ").length < 3) {
      spectrum.copy(
        score = CurationUtilities.addImpact(spectrum.score, -2, "Too few ions for validation and similarity comparisons")
      )
    } else if (spectrum.spectrum.split(" ").length > 250) {

      spectrum.copy(
        score = CurationUtilities.addImpact(spectrum.score, -2, "Excessive number of ions")
      )
    } else {
      spectrum
    }
  }
}
