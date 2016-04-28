package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.RemoveComputedData
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CalculateCompoundProperties
import org.springframework.batch.item.ItemProcessor

/**
  * Created by wohlgemuth on 3/14/16.
  */
@Step(description = "this step fetches all external compounds id's from the CTS system and updates MoNA with them", previousClass = classOf[CalculateCompoundProperties], workflow = "spectra-curation")
class FetchCompoundData extends ItemProcessor[Spectrum, Spectrum]{
  override def process(spectrum: Spectrum): Spectrum = {
    val updatedCompound: Array[Compound] = spectrum.compound.map(fetchCompoundData)

    // Assembled spectrum with updated compounds
    spectrum.copy(compound = updatedCompound)
  }

  def fetchCompoundData(compound: Compound): Compound = {
    compound
  }
}