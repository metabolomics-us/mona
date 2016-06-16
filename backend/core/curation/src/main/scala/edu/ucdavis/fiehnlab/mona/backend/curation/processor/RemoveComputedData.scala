package edu.ucdavis.fiehnlab.mona.backend.curation.processor

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.batch.item.ItemProcessor

/**
  * Created by wohlgemuth on 3/11/16.
  */
@Step(description = "this step will remove all computed metadata, names and tags from the given spectrum", workflow = "spectra-curation")
class RemoveComputedData extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  /**
    * processes the given spectrum and removes all it's computed meta data
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    logger.info(s"Filtering computed data in spectrum ${spectrum.id}")

    val filteredCompound: Array[Compound] =
      if (spectrum.compound != null) {
        spectrum.compound.map(compound =>
          compound.copy(
            classification = filterMetaData(compound.classification),
            metaData = filterMetaData(compound.metaData),
            names = filterNames(compound.names),
            tags = filterTags(compound.tags)
          )
        )
      } else {
        Array()
      }

    val filteredMetaData: Array[MetaData] = filterMetaData(spectrum.metaData)
    val filteredTags: Array[Tags] = filterTags(spectrum.tags)

    logger.info(s"Filtered metadata from ${spectrum.metaData.length} -> ${filteredMetaData.length}")
    logger.info(s"Filtered tags from ${spectrum.tags.length} -> ${filteredTags.length}")

    // Assembled filtered spectrum
    spectrum.copy(
      compound = filteredCompound,
      metaData = filteredMetaData,
      splash = null,
      score = null,
      tags = filteredTags
    )
  }

  /**
    * only keep metadata where computed = false
    *
    * @param metaData
    * @return
    */
  def filterMetaData(metaData: Array[MetaData]) : Array[MetaData] = {
    if(metaData != null) {
      metaData.filter(!_.computed)
    } else {
      Array()
    }
  }

  /**
    * only keep names where computed = false
    *
    * @param names
    * @return
    */
  def filterNames(names: Array[Names]) : Array[Names] = {
    if(names != null) {
      names.filter(!_.computed)
    } else {
      Array()
    }
  }

  /**
    * only keep metadata where ruleBased = false
    *
    * @param tags
    * @return
    */
  def filterTags(tags: Array[Tags]) : Array[Tags] = {
    if(tags != null) {
      tags.filter(!_.ruleBased)
    } else {
      Array()
    }
  }
}

object RemoveComputedData {
  def apply: RemoveComputedData = new RemoveComputedData()
}
