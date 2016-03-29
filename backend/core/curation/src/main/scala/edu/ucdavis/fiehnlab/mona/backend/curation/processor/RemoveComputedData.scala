package edu.ucdavis.fiehnlab.mona.backend.curation.processor

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Names, Spectrum, Tags}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.batch.item.ItemProcessor

/**
  * Created by wohlgemuth on 3/11/16.
  */
@Step(description = "this step will remove all computed metadata, names and tags from the given spectrum", workflow = "spectra-curration")
class RemoveComputedData extends ItemProcessor[Spectrum, Spectrum]{

  /**
    * processes the given spectrum and removes all it's computed meta data
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {

    val filteredBiologicalCompound =
      if (spectrum.biologicalCompound != null)
        spectrum.biologicalCompound.copy(
          metaData = filterMetaData(spectrum.biologicalCompound.metaData),
          names = filterNames(spectrum.biologicalCompound.names),
          tags = filterTags(spectrum.biologicalCompound.tags)
        )
      else null

    val filteredChemicalCompound =
      if (spectrum.biologicalCompound != null)
        spectrum.chemicalCompound.copy(
          metaData = filterMetaData(spectrum.chemicalCompound.metaData),
          names = filterNames(spectrum.chemicalCompound.names),
          tags = filterTags(spectrum.chemicalCompound.tags)
        )
      else null

    // assembled filtered spectrum
    spectrum.copy(
      biologicalCompound = filteredBiologicalCompound,
      chemicalCompound = filteredChemicalCompound,
      metaData = filterMetaData(spectrum.metaData),
      tags = filterTags(spectrum.tags)
    )
  }

  /**
    * only keep metadata where computed = false
    *
    * @param metaData
    * @return
    */
  def filterMetaData(metaData: Array[MetaData]) : Array[MetaData] = metaData.filter(!_.computed)

  /**
    * only keep names where computed = false
    *
    * @param names
    * @return
    */
  def filterNames(names: Array[Names]) : Array[Names] = names.filter(!_.computed)

  /**
    * only keep metadata where ruleBased = false
    *
    * @param tags
    * @return
    */
  def filterTags(tags: Array[Tags]) : Array[Tags] = tags.filter(!_.ruleBased)
}
