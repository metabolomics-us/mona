package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical.AdductBuilder
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.ArrayBuffer

@Step(description = "this step will calculate all possible adducts")
class CalculateAllAdducts extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  private val PRECURSOR_MATCH_TOLERANCE: Double = 0.5

  /**
   * processes the given spectrum
   *
   * @param spectrum to be processed
   * @return processed spectrum
   */
  override def process(spectrum: Spectrum): Spectrum = {
    // Get computed total exact mass from the biological compound if it exists
    if(spectrum.tags.exists(x => x.text == "GC-MS")) {
      logger.info(s"Process is only for LC-MS spectra")
      spectrum
    } else {
      val biologicalIndex: Int =
        if (spectrum.compound.exists(_.kind == "biological")) {
          spectrum.compound.indexWhere(_.kind == "biological")
        } else if (spectrum.compound.nonEmpty) {
          0
        } else {
          -1
        }

      if (biologicalIndex == -1) {
        logger.info(s"${spectrum.id}: Valid compound could not be found!")
        spectrum
      } else {
        // Get total exact mass from biological compound
        val theoreticalMass: Double = {
          val x: String = CurationUtilities.findMetaDataValue(spectrum.compound(biologicalIndex).metaData, CommonMetaData.TOTAL_EXACT_MASS)

          if (x == null) {
            -1
          } else {
            x.toDouble
          }
        }

        val biologicalCompound = spectrum.compound(biologicalIndex)
        val adductMap = AdductBuilder.LCMS_POSITIVE_ADDUCTS ++ AdductBuilder.LCMS_NEGATIVE_ADDUCTS

        val updatedMetadata: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()
        biologicalCompound.metaData.foreach(x => updatedMetadata.append(x))

        val updatedCompoundSet: ArrayBuffer[Compound] = new ArrayBuffer[Compound]()
        spectrum.compound.foreach(x => updatedCompoundSet.append(x))

        if (theoreticalMass < 0) {
          logger.info(s"${spectrum.id}: Computed exact mass was not found, unable to generate adduct information")
          spectrum
        } else {
          adductMap.foreach {
            case (x, f) => updatedMetadata.append(MetaData("theoretical adduct", true, true, x, null, null, null, f(theoreticalMass)))
          }

          val updatedCompound = biologicalCompound.copy(
            metaData = updatedMetadata.toArray
          )

          updatedCompoundSet.update(biologicalIndex, updatedCompound)

          spectrum.copy(
            compound = updatedCompoundSet.toArray
          )
        }
      }
    }
  }
}
