package edu.ucdavis.fiehnlab.mona.backend.curation.processor.instrument

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum, Tags}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.common.{CommonMetaData, CommonTags}
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 3/16/16.
  */
@Step(description = "this step will identify the given spectrum as GC/MS or LC/MS")
class IdentifyChromatography extends ItemProcessor[Spectrum,Spectrum] with LazyLogging {

  /**
    * criteria for GC/MS identification
    */
  val GCMS_CRITERIA: Map[String, Array[String]] = Map(
    (CommonMetaData.INSTRUMENT, Array(".*gcms.*", ".*gc-ms.*", ".*gc/ms.*")),
    (CommonMetaData.INSTRUMENT_TYPE, Array(".*gc.*", "ei-b")),
    (CommonMetaData.IONIZATION_ENERGY, Array("ev"))
  )

  /**
    * criteria for LC/MS identification
    */
  val LCMS_CRITERIA: Map[String, Array[String]] = Map(
    (CommonMetaData.INSTRUMENT, Array(".*lcms.*", ".*lc-ms.*", ".*lc/ms.*", ".*ltq.*")),
    (CommonMetaData.INSTRUMENT_TYPE, Array(".*lc.*")),
    (CommonMetaData.SOLVENT, Array(".*")),
    ("*", Array(".*direct infusion.*"))
  )


  /**
    * processes the given spectrum and
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    val tags = spectrum.tags.filter(x => x.text == CommonTags.GCMS_SPECTRUM || x.text == CommonTags.LCMS_SPECTRUM)

    if (tags.length == 1) {
      logger.info(s"Spectrum ${spectrum.id} already has identified chromotography: ${tags(0).text}")
      spectrum
    }

    else if (tags.length > 1) {
      logger.warn(s"Spectrum ${spectrum.id} has multiple chromotography tags!")
      spectrum
    }

    else {
      val isGCMS = isGCMSSpectrum(spectrum)
      val isLCMS = isLCMSSpectrum(spectrum)

      if (isGCMS && isLCMS) {
        logger.warn(s"Spectrum ${spectrum.id} was identified as both GC/MS and LC/MS!")
        spectrum
      } else if (isGCMS) {
        logger.info(s"Identified spectrum ${spectrum.id} as GC/MS")

        // Add GCMS tag
        val updatedTags = spectrum.tags :+ new Tags(true, CommonTags.GCMS_SPECTRUM)
        spectrum.copy(tags = updatedTags)
      } else if (isLCMS) {
        logger.info(s"Identified spectrum ${spectrum.id} as LC/MS")

        // Add LCMS tag
        val updatedTags = spectrum.tags :+ new Tags(true, CommonTags.LCMS_SPECTRUM)
        spectrum.copy(tags = updatedTags)
      } else {
        logger.warn(s"Spectrum ${spectrum.id} has unidentifiable chromotography")
        spectrum
      }
    }
  }


  /**
    * check if a spectrum is GC/MS
    *
    * @param spectrum
    * @return
    */
  def isGCMSSpectrum(spectrum: Spectrum): Boolean = {
    spectrum.metaData.exists(validateMetaData(_, GCMS_CRITERIA))
  }

  /**
    * check if a spectrum is LC/MS
    *
    * @param spectrum
    * @return
    */
  def isLCMSSpectrum(spectrum: Spectrum): Boolean = {
    spectrum.metaData.exists(validateMetaData(_, LCMS_CRITERIA))
  }


  /**
    *
    * @param metaData
    * @return
    */
  def validateMetaData(metaData: MetaData, criteria: Map[String, Array[String]]): Boolean = {
    criteria.exists(x =>
      // Check that the metadata name matches the name criterion
      if (x._1 == "*" || metaData.name.toLowerCase == x._1.toLowerCase) {
        logger.trace(s"MetaData name ${metaData.name} matches criterion ${x._1}")

        // Check that the metadata value matches the value criteria
        x._2.exists(y =>
          if (metaData.value.toString.toLowerCase == y.toLowerCase) {
            logger.trace(s"MetaData value ${metaData.value} matches value criterion ${y}")
            true
          } else if (metaData.unit != null && metaData.unit.toLowerCase == y.toLowerCase) {
            logger.trace(s"MetaData value ${metaData.value} matches unit criterion ${y}")
            true
          } else if (metaData.value.toString.matches("(?i)"+ y)) {
            logger.trace(s"MetaData value ${metaData.value} matches regex criterion ${y}")
            true
          } else {
            false
          }
        )
      } else {
        false
      }
    )
  }
}