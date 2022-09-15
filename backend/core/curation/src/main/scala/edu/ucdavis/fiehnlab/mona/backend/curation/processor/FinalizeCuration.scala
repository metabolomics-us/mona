package edu.ucdavis.fiehnlab.mona.backend.curation.processor

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{Score, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.batch.item.ItemProcessor
import scala.jdk.CollectionConverters._

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

    spectrum.setLastCurated(new Date().toString)
    spectrum.setScore(score)
    spectrum
  }
}
