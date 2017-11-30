package edu.ucdavis.fiehnlab.mona.backend.curation.processor.legacy

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{LegacySpectrum, Spectrum}
import org.springframework.batch.item.ItemProcessor

/**
  * converts the mona legacy format into the new format
  */
class LegacyProcessor extends ItemProcessor[LegacySpectrum, Spectrum] {

  /**
    * simple wrapper
    *
    * @param i
    * @return
    */
  override def process(i: LegacySpectrum): Spectrum = i.asSpectrum
}
