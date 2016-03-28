package edu.ucdavis.fiehnlab.mona.backend.curation.processor.cts

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.batch.item.ItemProcessor

/**
  * Created by wohlgemuth on 3/14/16.
  */
@Step(description = "this fetches all names from the CTS system and updates MoNA with them",previousClass = classOf[FetchCompoundIds],workflow = "spectra-curration")
class FetchCompoundNames extends ItemProcessor[Spectrum,Spectrum] {

  override def process(i: Spectrum): Spectrum = {
    i
  }
}
