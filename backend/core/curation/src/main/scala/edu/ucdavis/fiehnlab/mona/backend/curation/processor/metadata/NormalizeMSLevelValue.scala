package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Score, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import org.springframework.batch.item.ItemProcessor
import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters._
/**
  * Created by sajjan on 4/16/16.
  */
@Step(description = "this step will update ms level metadata values to standard values")
class NormalizeMSLevelValue extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    val updatedMetaData: Buffer[MetaData] = spectrum.getMetaData.asScala.map(normalizeMSLevelData(_, spectrum.getId)).filter(_ != null)

    val updatedScore: Score =
      if (updatedMetaData.exists(x => x.getName.toLowerCase == CommonMetaData.MS_LEVEL.toLowerCase && x.getValue.toString.matches("^MS[1-9]$"))) {
        CurationUtilities.addImpact(spectrum.getScore, 1, "MS type/level identified")
      } else {
        CurationUtilities.addImpact(spectrum.getScore, -1, "No MS type/level provided")
      }

    spectrum.setMetaData(updatedMetaData.asJava)
    spectrum.setScore(updatedScore)
    spectrum
  }

  /**
    * rename metadata names according to a provided synonym list
    *
    * @param metaData
    * @return
    */
  def normalizeMSLevelData(metaData: MetaData, id: String): MetaData = {
    if (metaData.getName == CommonMetaData.MS_LEVEL) {
      val value: String = metaData.getValue.toString.trim

      logger.debug(s"$id: Found MS level metadata with value: $value")

      if (value == "n/a") {
        logger.warn(s"$id: MS level with value '$value' deemed invalid - removing metadata value")
        null
      }

      else if ("^MS[1-9]$".r.findFirstIn(value).isDefined) {
        logger.info(s"$id: MS level with value '$value' requires no modifications")
        metaData
      }

      else if ("^ms[1-9]$".r.findFirstIn(value.toLowerCase()).isDefined) {
        logger.info(s"$id: Identified MS level value '$value' as ${value.toUpperCase}")
        metaData.setValue(value.toUpperCase)
        metaData
      }

      else if (value.toLowerCase == "ms") {
        logger.info(s"$id: Identified MS level value '$value' as MS1")
        metaData.setValue("MS1")
        metaData
      }

      else if (value.toLowerCase == "msms" || value.toLowerCase == "ms/ms") {
        logger.info(s"$id: Identified MS level value '$value' as MS2")
        metaData.setValue("MS2")
        metaData
      }

      else if ("^[1-9]$".r.findFirstIn(value).isDefined) {
        logger.info(s"$id: Identified MS level value '$value' as MS$value")
        metaData.setValue(s"MS$value")
        metaData
      }

      else {
        logger.warn(s"$id: MS level value '$value' was unidentifiable - keeping metadata value")
        metaData
      }
    } else {
      metaData
    }
  }
}
