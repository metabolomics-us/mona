package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CurationUtilities
import org.springframework.batch.item.ItemProcessor

@Step(description = "this step will score the spectrum based on the number of ions present")
class SpectrumIonCountScoringRule  extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {

    if (spectrum.getSpectrum.split(" ").length < 3) {
      spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, -2, "Too few ions for validation and similarity comparisons"))
      spectrum
    } else if (spectrum.getSpectrum.split(" ").length > 1000) {
      spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, -2, "Excessive number of ions"))
      spectrum
    } else {
      spectrum
    }
  }
}
