package edu.ucdavis.fiehnlab.mona.backend.curation.processor

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Score, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.batch.item.ItemProcessor

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
    logger.info(s"${spectrum.id}: Finalizing curation")

    // Update score object with scaled score if possible, otherwise score is null
    val score: Score =
      if (spectrum.score.impacts.nonEmpty) {
        val rawScore: Double = spectrum.score.impacts.map(_.value).sum
        val totalScore: Double = spectrum.score.impacts.map(_.value.abs).sum

        if (totalScore == 0) {
          // Give a median score if no impacts are given
          spectrum.score.copy(score = 2.5)
        } else {
          spectrum.score.copy(score = 2.5 * (1 + rawScore / totalScore))
        }
      } else {
        null
      }

    spectrum.copy(lastCurated = new Date, score = score)
  }
}
