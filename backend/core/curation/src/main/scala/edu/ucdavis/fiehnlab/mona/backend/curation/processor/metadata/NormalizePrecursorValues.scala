package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{MetaDataDAO, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.{ArrayBuffer, Buffer}
import scala.jdk.CollectionConverters._
import scala.util.Try

/**
  * Created by sajjan on 2/4/19.
  */
@Step(description = "this step will update precursor m/z and type in MS^n spectra")
class NormalizePrecursorValues extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  val INVALID_PRECURSOR_TYPES: Array[String] = Array("", "--", "MS/MS")


  /**
   * processes the given spectrum and removes all it's computed meta data
   *
   * @param spectrum to be processed
   * @return processed spectrum
   */
  override def process(spectrum: Spectrum): Spectrum = {

    // All metadata not related to precursors
    val metaData: Buffer[MetaDataDAO] = spectrum.getMetaData.asScala
      .filter(x => x.getName.toLowerCase != CommonMetaData.PRECURSOR_MASS.toLowerCase && x.getName.toLowerCase != CommonMetaData.PRECURSOR_TYPE.toLowerCase)


    val precursorMzMatch: Buffer[MetaDataDAO] = spectrum.getMetaData.asScala.filter(_.getName.toLowerCase == CommonMetaData.PRECURSOR_MASS.toLowerCase)
    val precursorTypeMatch: Buffer[MetaDataDAO] = spectrum.getMetaData.asScala
      .filter(_.getName.toLowerCase == CommonMetaData.PRECURSOR_TYPE.toLowerCase)
      .filter(x => !INVALID_PRECURSOR_TYPES.contains(x.getValue.toString.trim))


    if (precursorMzMatch.nonEmpty && precursorMzMatch.exists(_.getValue.toString.contains('/'))) {
      // If any precursor value has a / in it, multiple MS levels must be represented
      // Duplicate precursor values and replace primary values with the last (parent) value
      val updatedMetaData: ArrayBuffer[MetaDataDAO] = new ArrayBuffer[MetaDataDAO]()
      var invalidPrecursorMz: Boolean = false

      precursorMzMatch.foreach {
        x =>
          val meta = new MetaDataDAO(x)
          meta.setName("original " + CommonMetaData.PRECURSOR_MASS)
          updatedMetaData.append(meta)
      }
      precursorTypeMatch.foreach {
        x =>
          val meta = new MetaDataDAO(x)
          meta.setName("original " + CommonMetaData.PRECURSOR_TYPE)

          updatedMetaData.append(meta)
      }

      precursorMzMatch.foreach { x =>
        // Split precursor m/z and convert to double if possible
        val mzString = x.getValue.toString.split('/').last

        parseDouble(mzString) match {
          case Some(mz) =>
            val meta = new MetaDataDAO(x)
            meta.setValue(mz.toString)
            updatedMetaData.append(meta)
          case None =>
            val meta = new MetaDataDAO(x)
            meta.setValue(mzString)
            updatedMetaData.append(meta)
            invalidPrecursorMz = true
        }
      }

      precursorTypeMatch.foreach { x =>
        val meta = new MetaDataDAO(x)
        meta.setValue(meta.getValue.split('/').last)
        updatedMetaData.append(meta)
      }

      // Update spectrum object with new metadata and score if needed
      spectrum.setMetaData((metaData ++ updatedMetaData).asJava)
      val score = {
        if (invalidPrecursorMz)
          CurationUtilities.addImpact(spectrum.getScore, -1, "provided precursor m/z is not valid")
        else
          spectrum.getScore
      }
      spectrum.setScore(score)
      spectrum
    }
    else
    {
      // Filter invalid precursor types
      spectrum.setMetaData((metaData ++ precursorMzMatch ++ precursorTypeMatch).asJava)
      spectrum
    }
  }

    def parseDouble(s: String): Option[Double] = Try {
      s.toDouble
    }.toOption
}
