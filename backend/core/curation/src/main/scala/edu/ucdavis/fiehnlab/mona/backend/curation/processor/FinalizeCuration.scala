package edu.ucdavis.fiehnlab.mona.backend.curation.processor

import java.util.Date
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Score, Spectrum, Tag}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.batch.item.ItemProcessor

import scala.jdk.CollectionConverters._
import scala.collection.mutable.Buffer

/**
  * Created by sajjan on 2/14/17.
  */
@Step(description = "this step finalizes the curation workflow and computed the spectrum score", workflow = "spectra-curation")
class FinalizeCuration extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  /**
    * processes the given spectrum and finalizes the curation workflow
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    logger.info(s"${spectrum.getId}: Finalizing curation")

    // Update score object with scaled score if possible, otherwise score is null
    val score: Score =
      if (spectrum.getScore.getImpacts.asScala.nonEmpty) {
        val rawScore: Double = spectrum.getScore.getImpacts.asScala.map(_.getValue.toDouble).sum
        val totalScore: Double = spectrum.getScore.getImpacts.asScala.map(_.getValue.toDouble.abs).sum

        if (totalScore == 0) {
          // Give a median score if no impacts are given
          spectrum.getScore.setScore(2.5)
          spectrum.getScore
        } else {
          spectrum.getScore.setScore(2.5 * (1 + rawScore / totalScore))
          spectrum.getScore
        }
      } else {
        null
      }

    // Add In-Silico Tag to LipidBlast 2022 spectra
    val updatedTags: Buffer[Tag] =
      if (spectrum.getTags.asScala.exists(_.getText == "LipidBlast 2022"))
        spectrum.getTags.asScala :+ new Tag("In-Silico", false)
      else
        spectrum.getTags.asScala

    spectrum.setTags(updatedTags.asJava)
    spectrum.setLastCurated(new Date())
    spectrum.setScore(score)
    spectrum
  }
}
