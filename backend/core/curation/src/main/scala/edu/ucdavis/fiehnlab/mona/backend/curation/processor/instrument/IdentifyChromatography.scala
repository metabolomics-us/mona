package edu.ucdavis.fiehnlab.mona.backend.curation.processor.instrument

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Impact, MetaData, Spectrum, Tags}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CommonTags, CurationUtilities}
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 3/16/16.
  */
@Step(description = "this step will identify the given spectrum as GC/MS or LC/MS")
class IdentifyChromatography extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  /**
    * Criteria for GC/MS identification
    */
  val GCMS_METADATA_CRITERIA: Map[String, Array[String]] = Map(
    (CommonMetaData.INSTRUMENT, Array(".*gcms.*", ".*gc-ms.*", ".*gc/ms.*")),
    (CommonMetaData.INSTRUMENT_TYPE, Array(".*gc.*", "ei-b", "ci-b")),
    (CommonMetaData.IONIZATION_ENERGY, Array("ev")),
    (CommonMetaData.SAMPLE_INTRODUCTION, Array(".*gc.*", ".*gas.*"))
  )

  /**
    * Criteria for LC/MS identification
    */
  val LCMS_METADATA_CRITERIA: Map[String, Array[String]] = Map(
    (CommonMetaData.INSTRUMENT, Array(".*lcms.*", ".*lc-ms.*", ".*lc/ms.*", ".*ltq.*")),
    (CommonMetaData.INSTRUMENT_TYPE, Array(".*lc.*", ".*quattro[ _]qqq.*")),
    (CommonMetaData.SOLVENT, Array(".*")),
    (CommonMetaData.SAMPLE_INTRODUCTION, Array(".*lc.*", ".*liquid.*")),
    ("ion source", Array(".*lc.*")),
    ("*", Array(".*direct infusion.*", ".*direct injection.*", "flow-injection", "flow injection"))
  )

  /**
    * Criteria for CE/MS identification
    */
  val CEMS_METADATA_CRITERIA: Map[String, Array[String]] = Map(
    (CommonMetaData.INSTRUMENT, Array("ce-.*")),
    (CommonMetaData.INSTRUMENT_TYPE, Array("ce-.*"))
  )


  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    val tags: Array[Tags] = spectrum.tags.filter(x => Array(CommonTags.GCMS_SPECTRUM, CommonTags.LCMS_SPECTRUM, CommonTags.CEMS_SPECTRUM).contains(x.text))

    if (tags.length == 1) {
      logger.info(s"${spectrum.id}: Spectrum already has identified chromatography: ${tags(0).text}")


      spectrum.copy(score = CurationUtilities.addImpact(spectrum.score, -1, s"Chromatography identified as ${tags(0).text}"))
    }

    else if (tags.length > 1) {
      logger.warn(s"${spectrum.id}: Spectrum has multiple chromatography tags!")

      spectrum.copy(score = CurationUtilities.addImpact(spectrum.score, -1, s"Chromatography identified with multiple tags: ${tags.map(_.text).mkString(", ")}"))
    }

    else {
      val isGCMS = spectrum.metaData.exists(validateMetaData(_, GCMS_METADATA_CRITERIA, spectrum.id))
      val isLCMS = spectrum.metaData.exists(validateMetaData(_, LCMS_METADATA_CRITERIA, spectrum.id))
      val isCEMS = spectrum.metaData.exists(validateMetaData(_, CEMS_METADATA_CRITERIA, spectrum.id))


      if (isGCMS && isLCMS) {
        logger.warn(s"${spectrum.id}: Identified as both GC/MS and LC/MS!")
        spectrum.copy(score = spectrum.score.copy(impacts = spectrum.score.impacts :+ Impact(-1, "Identified as both GC/MS and LC/MS")))
        spectrum.copy(score = CurationUtilities.addImpact(spectrum.score, -1, "Chromatography identified as both GC/MS and LE/MS"))
      }

      else if (isGCMS && isCEMS) {
        logger.warn(s"${spectrum.id}: Identified as both GC/MS and CE/MS!")
        spectrum.copy(score = CurationUtilities.addImpact(spectrum.score, -1, "Chromatography identified as both GC/MS and CE/MS"))
      }

      else if (isLCMS && isCEMS) {
        logger.warn(s"${spectrum.id}: Identified as both LC/MS and CE/MS!")
        spectrum.copy(score = CurationUtilities.addImpact(spectrum.score, -1, "Chromatography identified as both LC/MS and CE/MS"))
      }

      else if (isGCMS) {
        logger.info(s"${spectrum.id}: Identified as GC/MS")

        // Add GCMS tag and metadata
        val updatedTags: Array[Tags] =
          if (spectrum.tags.exists(_.text == CommonTags.GCMS_SPECTRUM))
            spectrum.tags
          else
            spectrum.tags :+ Tags(ruleBased = true, CommonTags.GCMS_SPECTRUM)

        spectrum.copy(
          tags = updatedTags,
          score = CurationUtilities.addImpact(spectrum.score, 1, "Chromatography identified as GC-MS")
        )
      }

      else if (isLCMS) {
        logger.info(s"${spectrum.id}: Identified as LC/MS")

        // Add LCMS tag
        val updatedTags: Array[Tags] =
          if (spectrum.tags.exists(_.text == CommonTags.LCMS_SPECTRUM))
            spectrum.tags
          else
            spectrum.tags :+ Tags(ruleBased = true, CommonTags.LCMS_SPECTRUM)

        spectrum.copy(
          tags = updatedTags,
          score = CurationUtilities.addImpact(spectrum.score, 1, "Chromatography identified as LC-MS")
        )
      }

      else if (isCEMS) {
        logger.info(s"${spectrum.id}: Identified as CE/MS")

        // Add CEMS tag
        val updatedTags: Array[Tags] =
          if (spectrum.tags.exists(_.text == CommonTags.CEMS_SPECTRUM))
            spectrum.tags
          else
            spectrum.tags :+ Tags(ruleBased = true, CommonTags.CEMS_SPECTRUM)

        spectrum.copy(
          tags = updatedTags,
          score = CurationUtilities.addImpact(spectrum.score, 1, "Chromatography identified as CD-MS")

        )
      }

      else {
        logger.warn(s"${spectrum.id}: Unidentifiable chromatography")
        spectrum.copy(score = CurationUtilities.addImpact(spectrum.score, -1, "Unidentifiable chromatography"))
      }
    }
  }


  /**
    *
    * @param metaData
    * @return
    */
  def validateMetaData(metaData: MetaData, criteria: Map[String, Array[String]], id: String): Boolean = {
    criteria.exists {
      case (name: String, terms: Array[String]) =>
        // Check that the metadata name matches the name criterion
        if (name == "*" || metaData.name.toLowerCase == name.toLowerCase) {
          logger.debug(s"$id: MetaData name ${metaData.name} matches criterion $name")

          // Check that the metadata value matches the value criteria
          terms.exists(term =>
            if (metaData.value.toString.toLowerCase == term.toLowerCase) {
              logger.info(s"$id: MetaData value ${metaData.value} matches value criterion $term")
              true
            }

            else if (metaData.unit != null && metaData.unit.toLowerCase == term.toLowerCase) {
              logger.info(s"$id: MetaData value ${metaData.value} matches unit criterion $term")
              true
            }

            else if (metaData.value.toString.toLowerCase.matches(term)) {
              logger.info(s"$id: MetaData value ${metaData.value} matches regex criterion $term")
              true
            }

            else {
              false
            }
          )
        }

        else {
          false
        }

      case _ => false
    }
  }
}