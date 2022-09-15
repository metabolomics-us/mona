package edu.ucdavis.fiehnlab.mona.backend.curation.processor

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao._
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.batch.item.ItemProcessor
import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters._

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
    logger.info(s"${spectrum.getId}: Filtering computed data")

    val filteredCompound: Buffer[CompoundDAO] =
      if (spectrum.getCompound != null) {
        spectrum.getCompound.asScala.map { compound =>
          compound.setMetaData(filterMetaData(compound.getMetaData.asScala).asJava)
          compound.setNames(filterNames(compound.getNames.asScala).asJava)
          compound.setTags(filterTags(compound.getTags.asScala).asJava)
          compound
        }
      } else {
        Buffer[CompoundDAO]()
      }

    val filteredMetaData: Buffer[MetaDataDAO] = filterMetaData(spectrum.getMetaData.asScala)
    val filteredTags: Buffer[TagDAO] = filterTags(spectrum.getTags.asScala)

    logger.info(s"${spectrum.getId}: Filtered metadata from ${Option(spectrum.getMetaData.asScala).getOrElse(Buffer[MetaDataDAO]()).length} -> ${filteredMetaData.length}")
    logger.info(s"${spectrum.getId}: Filtered tags from ${Option(spectrum.getTags.asScala).getOrElse(Buffer[TagDAO]()).length} -> ${filteredTags.length}")

    // Assembled filtered spectrum
    spectrum.setCompound(filteredCompound.asJava)
    spectrum.setMetaData(filteredMetaData.asJava)
    spectrum.setSplash(null)
    spectrum.setScore(new Score(Buffer[Impacts]().asJava, 0.0, 0.0, 0.0))
    spectrum.setTags(filteredTags.asJava)
    spectrum
  }

  /**
    * only keep metadata where computed = false
    *
    * @param metaData
    * @return
    */
  def filterMetaData(metaData: Buffer[MetaDataDAO]): Buffer[MetaDataDAO] = {
    if (metaData != null) {
      metaData.filter(!_.getComputed)
    } else {
      Buffer[MetaDataDAO]()
    }
  }

  /**
    * only keep names where computed = false
    *
    * @param names
    * @return
    */
  def filterNames(names: Buffer[Names]): Buffer[Names] = {
    if (names != null) {
      names.filter(!_.getComputed)
    } else {
      Buffer[Names]()
    }
  }

  /**
    * only keep metadata where ruleBased = false
    *
    * @param tags
    * @return
    */
  def filterTags(tags: Buffer[TagDAO]): Buffer[TagDAO] = {
    if (tags != null) {
      tags.filter(!_.getRuleBased)
    } else {
      Buffer[TagDAO]()
    }
  }
}

object RemoveComputedData {
  def apply: RemoveComputedData = new RemoveComputedData()
}
