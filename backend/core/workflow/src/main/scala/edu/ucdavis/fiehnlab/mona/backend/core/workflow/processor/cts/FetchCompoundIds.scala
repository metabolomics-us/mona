package edu.ucdavis.fiehnlab.mona.backend.core.workflow.processor.cts

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.processor.RemoveComputedMetaData
import org.springframework.batch.item.ItemProcessor

/**
  * Created by wohlgemuth on 3/14/16.
  */
@Step(description = "this fetches all external compounds id's from the CTS system and updates MoNA with them",previousClass = classOf[RemoveComputedMetaData])
class FetchCompoundIds extends ItemProcessor[Spectrum,Spectrum]{
  override def process(i: Spectrum): Spectrum = ???
}
