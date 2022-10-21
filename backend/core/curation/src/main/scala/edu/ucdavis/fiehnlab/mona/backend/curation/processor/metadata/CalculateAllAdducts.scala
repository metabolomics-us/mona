package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{Spectrum, CompoundDAO, MetaDataDAO}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical.AdductBuilder
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CurationUtilities}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

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
    if(spectrum.getTags.asScala.exists(x => x.getText == "GC-MS")) {
      logger.info(s"Process is only for LC-MS spectra")
      spectrum
    } else {
      val biologicalIndex: Int =
        if (spectrum.getCompound.asScala.exists(_.getKind == "biological")) {
          spectrum.getCompound.asScala.indexWhere(_.getKind == "biological")
        } else if (spectrum.getCompound.asScala.nonEmpty) {
          0
        } else {
          -1
        }

      if (biologicalIndex == -1) {
        logger.info(s"${spectrum.getId}: Valid compound could not be found!")
        spectrum
      } else {
        // Get total exact mass from biological compound
        val theoreticalMass: Double = {
          val x: String = CurationUtilities.findMetaDataValue(spectrum.getCompound.get(biologicalIndex).getMetaData.asScala, CommonMetaData.TOTAL_EXACT_MASS)

          if (x == null) {
            -1
          } else {
            x.toDouble
          }
        }

        val biologicalCompound = spectrum.getCompound.get(biologicalIndex)
        val adductMap = AdductBuilder.LCMS_POSITIVE_ADDUCTS ++ AdductBuilder.LCMS_NEGATIVE_ADDUCTS

        val updatedMetadata: ArrayBuffer[MetaDataDAO] = new ArrayBuffer[MetaDataDAO]()
        biologicalCompound.getMetaData.asScala.foreach(x => updatedMetadata.append(x))

        val updatedCompoundSet: ArrayBuffer[CompoundDAO] = new ArrayBuffer[CompoundDAO]()
        spectrum.getCompound.asScala.foreach(x => updatedCompoundSet.append(x))

        if (theoreticalMass < 0) {
          logger.info(s"${spectrum.getId}: Computed exact mass was not found, unable to generate adduct information")
          spectrum
        } else {
          adductMap.foreach {
            case (x, f) => updatedMetadata.append(
              new MetaDataDAO(null, x, f(theoreticalMass).toString, true, "theoretical adduct", true, null)
            )
          }
          val newMetaData = biologicalCompound.getMetaData
          newMetaData.addAll(updatedMetadata.asJava)
          biologicalCompound.setMetaData(newMetaData)

          updatedCompoundSet.update(biologicalIndex, biologicalCompound)

          spectrum.setCompound(updatedCompoundSet.asJava)

          spectrum
        }
      }
    }
  }
}
