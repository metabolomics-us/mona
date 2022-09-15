package edu.ucdavis.fiehnlab.mona.backend.curation.processor.validation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{MetaDataDAO, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.springframework.batch.item.ItemProcessor

import scala.util.matching.Regex
import scala.jdk.CollectionConverters._
import scala.collection.mutable.Buffer

/**
  * Created by sajjan on 4/16/16.
  */
@Step(description = "this step will validate the provided column information")
class ColumnValidation extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  val DIAMETER_AND_LENGTH_REGEX: Regex = "(\\d+\\.?\\d*(?: [cmu]?m)?)(?:(?:\\s?[xX]\\s?)|(?: by ))(\\d+\\.?\\d*\\s?[cmu]?m)".r
  val LENGTH_REGEX: Regex = "(\\d+\\.?\\d*\\s?[cmu]?m)".r

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    val metaData: Buffer[MetaDataDAO] = spectrum.getMetaData.asScala.filter(_.getName == CommonMetaData.COLUMN)

    if (metaData.isEmpty) {
      logger.info(s"${spectrum.getId}: No column found")
    } else {
      metaData.foreach { x =>
        val diameterAndLengthDefined: Boolean = DIAMETER_AND_LENGTH_REGEX.findFirstIn(x.getValue.toString).isDefined
        val onlyLengthDefined: Boolean = LENGTH_REGEX.findFirstIn(x.getValue.toString).isDefined

        if (!diameterAndLengthDefined && !onlyLengthDefined) {
          logger.warn(s"${spectrum.getId}: Length and Diameter not specified")
        }
      }
    }

    spectrum
  }
}
