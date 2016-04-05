package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, Splash}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 3/20/16.
  */
@Step(description = "this step calculate the SPLASH for the given spectrum")
class CalculateSplash extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {
  /**
    * processes the given spectrum and
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    if (spectrum.splash == null) {
      val splash = SplashUtil.splash(spectrum.spectrum, SpectraType.MS)
      val blocks = splash.split('-')

      // Assembled spectrum with a new SPLASH
      spectrum.copy(
        splash = Splash(blocks(0), blocks(1), blocks(2), splash)
      )
    } else {
      spectrum
    }
  }
}
