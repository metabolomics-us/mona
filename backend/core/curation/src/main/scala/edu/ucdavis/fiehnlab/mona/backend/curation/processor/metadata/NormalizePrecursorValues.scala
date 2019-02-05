package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Score, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.ArrayBuffer
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
    val metaData: Array[MetaData] = spectrum.metaData
      .filter(x => x.name.toLowerCase != CommonMetaData.PRECURSOR_MASS.toLowerCase && x.name.toLowerCase != CommonMetaData.PRECURSOR_TYPE.toLowerCase)

    val precursorMzMatch: Array[MetaData] = spectrum.metaData.filter(_.name.toLowerCase == CommonMetaData.PRECURSOR_MASS.toLowerCase)
    val precursorTypeMatch: Array[MetaData] = spectrum.metaData
      .filter(_.name.toLowerCase == CommonMetaData.PRECURSOR_TYPE.toLowerCase)
      .filter(x => !INVALID_PRECURSOR_TYPES.contains(x.value.toString.trim))

    if (precursorMzMatch.nonEmpty && precursorMzMatch.exists(_.value.toString.contains('/'))) {
      // If any precursor value has a / in it, multiple MS levels must be represented
      // Duplicate precursor values and replace primary values with the last (parent) value
      val updatedMetaData: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()
      var invalidPrecursorMz: Boolean = false

      precursorMzMatch.foreach(x => updatedMetaData.append(x.copy(name = "original " + CommonMetaData.PRECURSOR_TYPE)))
      precursorTypeMatch.foreach(x => updatedMetaData.append(x.copy(name = "original " + CommonMetaData.PRECURSOR_MASS)))

      precursorMzMatch.foreach(x => {
        // Split precursor m/z and convert to double if possible
        val mzString = x.value.toString.split('/').last

        parseDouble(mzString) match {
          case Some(mz) => updatedMetaData.append(x.copy(value = mz))
          case None => {
            updatedMetaData.append(x.copy(value = mzString))
            invalidPrecursorMz = true
          }
        }
      })
      precursorTypeMatch.foreach(x => updatedMetaData.append(x.copy(value = x.value.toString.split('/').last)))

      // Update spectrum object with new metadata and score if needed
      spectrum.copy(
        metaData = metaData ++ updatedMetaData,
        score = if (invalidPrecursorMz) CurationUtilities.addImpact(spectrum.score, -1, "provided precursor m/z is not valid") else spectrum.score
      )
    } else {
      // Filter invalid precursor types
      spectrum.copy(metaData = metaData ++ precursorMzMatch ++ precursorTypeMatch)
    }
  }

  def parseDouble(s: String): Option[Double] = Try { s.toDouble }.toOption
}