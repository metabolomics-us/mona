package edu.ucdavis.fiehnlab.mona.backend.curation.processor.instrument

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Impacts, MetaData, Spectrum, Tag}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.util.{CommonMetaData, CommonTags, CurationUtilities}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters._

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
    (CommonMetaData.COLUMN, Array(".*uplc.*", ".*acquity.*")),
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
    val tags: Buffer[Tag] = spectrum.getTags.asScala.filter(x => Array(CommonTags.GCMS_SPECTRUM, CommonTags.LCMS_SPECTRUM, CommonTags.CEMS_SPECTRUM).contains(x.getText))

    if (tags.length == 1) {
      logger.info(s"${spectrum.getId}: Spectrum already has identified chromatography: ${tags(0).getText}")


      spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, -1, s"Chromatography identified as ${tags(0).getText}"))
      spectrum
    }

    else if (tags.length > 1) {
      logger.warn(s"${spectrum.getId}: Spectrum has multiple chromatography tags!")

      spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, -1, s"Chromatography identified with multiple tags: ${tags.map(_.getText).mkString(", ")}"))
      spectrum
    }

    else {
      val isGCMS = spectrum.getMetaData.asScala.exists(validateMetaData(_, GCMS_METADATA_CRITERIA, spectrum.getId))
      val isLCMS = spectrum.getMetaData.asScala.exists(validateMetaData(_, LCMS_METADATA_CRITERIA, spectrum.getId))
      val isCEMS = spectrum.getMetaData.asScala.exists(validateMetaData(_, CEMS_METADATA_CRITERIA, spectrum.getId))


      if (isGCMS && isLCMS) {
        logger.warn(s"${spectrum.getId}: Identified as both GC/MS and LC/MS!")
        val score = spectrum.getScore
        val impacts = score.getImpacts
        impacts.add(new Impacts(-1, "Identified as both GC/MS and LC/MS"))
        score.setImpacts(impacts)
        spectrum.setScore(CurationUtilities.addImpact(score, -1, "Chromatography identified as both GC/MS and LE/MS"))
        spectrum
      }

      else if (isGCMS && isCEMS) {
        logger.warn(s"${spectrum.getId}: Identified as both GC/MS and CE/MS!")
        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, -1, "Chromatography identified as both GC/MS and CE/MS"))
        spectrum
      }

      else if (isLCMS && isCEMS) {
        logger.warn(s"${spectrum.getId}: Identified as both LC/MS and CE/MS!")
        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, -1, "Chromatography identified as both LC/MS and CE/MS"))
        spectrum
      }

      else if (isGCMS) {
        logger.info(s"${spectrum.getId}: Identified as GC/MS")

        // Add GCMS tag and metadata
        val updatedTags: Buffer[Tag] =
          if (spectrum.getTags.asScala.exists(_.getText == CommonTags.GCMS_SPECTRUM))
            spectrum.getTags.asScala
          else
            spectrum.getTags.asScala :+ new Tag(CommonTags.GCMS_SPECTRUM, true)

        spectrum.setTags(updatedTags.asJava)

        val score = CurationUtilities.addImpact(spectrum.getScore, 1, "Chromatography identified as GC-MS")
        spectrum.setScore(score)
        spectrum
      }

      else if (isLCMS) {
        logger.info(s"${spectrum.getId}: Identified as LC/MS")

        // Add LCMS tag
        val updatedTags: Buffer[Tag] =
          if (spectrum.getTags.asScala.exists(_.getText == CommonTags.LCMS_SPECTRUM))
            spectrum.getTags.asScala
          else
            spectrum.getTags.asScala :+ new Tag(CommonTags.LCMS_SPECTRUM, true)

        spectrum.setTags(updatedTags.asJava)
        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, 1, "Chromatography identified as LC-MS"))
        spectrum
      }

      else if (isCEMS) {
        logger.info(s"${spectrum.getId}: Identified as CE/MS")

        // Add CEMS tag
        val updatedTags: Buffer[Tag] =
          if (spectrum.getTags.asScala.exists(_.getText == CommonTags.CEMS_SPECTRUM))
            spectrum.getTags.asScala
          else
            spectrum.getTags.asScala :+ new Tag(CommonTags.CEMS_SPECTRUM, true)

        spectrum.setTags(updatedTags.asJava)
        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, 1, "Chromatography identified as CD-MS"))
        spectrum
      }

      else {
        logger.warn(s"${spectrum.getId}: Unidentifiable chromatography")
        spectrum.setScore(CurationUtilities.addImpact(spectrum.getScore, -1, "Unidentifiable chromatography"))
        spectrum
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
        if (name == "*" || metaData.getName.toLowerCase == name.toLowerCase) {
          logger.debug(s"$id: MetaData name ${metaData.getName} matches criterion $name")

          // Check that the metadata value matches the value criteria
          terms.exists(term =>
            if (metaData.getValue.toString.toLowerCase == term.toLowerCase) {
              logger.info(s"$id: MetaData value ${metaData.getValue} matches value criterion $term")
              true
            }

            else if (metaData.getUnit != null && metaData.getUnit.toLowerCase == term.toLowerCase) {
              logger.info(s"$id: MetaData value ${metaData.getValue} matches unit criterion $term")
              true
            }

            else if (metaData.getValue.toString.toLowerCase.matches(term)) {
              logger.info(s"$id: MetaData value ${metaData.getValue} matches regex criterion $term")
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
