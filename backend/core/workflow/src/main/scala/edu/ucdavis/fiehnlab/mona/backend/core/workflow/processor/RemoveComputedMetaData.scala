package edu.ucdavis.fiehnlab.mona.backend.core.workflow.processor

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.{MetaData, Spectrum}
import org.springframework.batch.item.ItemProcessor

/**
  * Created by wohlgemuth on 3/11/16.
  */
class RemoveComputedMetaData extends ItemProcessor[Spectrum,Spectrum]{

  /**
    * processes the given spectrum and removes all it's computed meta data
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {

    val metaDataRemoved = spectrum.copy(metaData = filterMetaData(spectrum.metaData))
    val processedBiologicalCompound = spectrum.biologicalCompound.copy(metaData = filterMetaData(spectrum.biologicalCompound.metaData))
    val processedChemicalCompound = spectrum.chemicalCompound.copy(metaData = filterMetaData(spectrum.chemicalCompound.metaData))

    //assemble return object
    metaDataRemoved.copy(biologicalCompound = processedBiologicalCompound, chemicalCompound = processedChemicalCompound, predictedCompound = null)
  }

  /**
    * only keep metadata where computed == false
    * @param metaData
    * @return
    */
  def filterMetaData(metaData:Array[MetaData]) : Array[MetaData] = metaData.filter(!_.computed)
}
