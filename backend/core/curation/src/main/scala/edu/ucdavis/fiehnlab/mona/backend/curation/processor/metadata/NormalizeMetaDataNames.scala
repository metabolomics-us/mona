package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.common.MetaDataSynonyms
import org.springframework.batch.item.ItemProcessor

/**
  * Created by wohlgemuth on 3/11/16.
  */
@Step(description = "this step will update metadata names to conform to a set of standards")
class NormalizeMetaDataNames extends ItemProcessor[Spectrum,Spectrum] {
  val SYNONYMS = MetaDataSynonyms.ALL_SYNONYMS

  /**
    * processes the given spectrum and removes all it's computed meta data
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    //assemble updated spectrum
    spectrum.copy(
      compound = spectrum.compound.map(x => x.copy(metaData = renameMetaData(x.metaData))),
      metaData = renameMetaData(spectrum.metaData)
    )
  }

  /**
    * rename metadata names according to a provided synonym list
    *
    * @param metaData
    * @return
    */
  def renameMetaData(metaData:Array[MetaData]) : Array[MetaData] = {
    metaData.map(x =>
      if (SYNONYMS.contains(x.name.toLowerCase))
        x.copy(name = SYNONYMS(x.name.toLowerCase))
      else
        x
    )
  }
}