package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{MetaDataDAO, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.MetaDataSynonyms
import org.springframework.batch.item.ItemProcessor
import scala.jdk.CollectionConverters._
import scala.collection.mutable.Buffer

/**
  * Created by wohlgemuth on 3/11/16.
  */
@Step(description = "this step will update metadata names to conform to a set of standards")
class NormalizeMetaDataNames extends ItemProcessor[Spectrum, Spectrum] {

  val SYNONYMS: Map[String, String] = MetaDataSynonyms.ALL_SYNONYMS

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    //assemble updated spectrum
    spectrum.setCompound(spectrum.getCompound.asScala.map{x =>
      x.setMetaData(renameMetaData(x.getMetaData.asScala).asJava)
      x
    }.asJava)
    spectrum.setMetaData(renameMetaData(spectrum.getMetaData.asScala).asJava)
    spectrum
  }

  /**
    * rename metadata names according to a provided synonym list
    *
    * @param metaData
    * @return
    */
  def renameMetaData(metaData: Buffer[MetaDataDAO]): Buffer[MetaDataDAO] = {
    metaData.map(x =>
      if (SYNONYMS.contains(x.getName.toLowerCase)) {
        x.setName(SYNONYMS(x.getName.toLowerCase))
        x
      } else {
        x
      }
    )
  }
}
