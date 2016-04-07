package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 4/4/16.
  */
@Step(description = "this step will convert the spectrum to a relative spectrum")
class NormalizeSpectrum extends ItemProcessor[Spectrum,Spectrum] with LazyLogging {

  /**
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    // Split the ions into m/z-intensity pairs
    val ions: Array[Array[String]] = spectrum.spectrum.split(' ').map(_.split(':'))

    // Determine the maximum intensity
    val maxIntensity: Double = ions.map(_(1).toDouble).max

    // Compute the relative spectrum
    val relativeSpectrum: String = ions.map(x => "%s:%0.6f".format(x(0), x(1).toDouble / maxIntensity * 100)).mkString(" ")

    // Replace the spectrum with the relative spectrum
    spectrum.copy(
      spectrum = relativeSpectrum
    )
  }
}