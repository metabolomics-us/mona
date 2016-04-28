package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.batch.item.ItemProcessor

/**
  * Created by sajjan on 3/01/16.
  */
@Step(description = "this step calculate the spectral quality for the given spectrum")
class CalculateSpectralQuality extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {
  /**
    * processes the given spectrum and
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    // Compute the descriptive features defined in Nesvizhskii et al (2006):
    // http://www.mcponline.org/content/5/4/652.full
    val ions: Array[Array[Double]] = spectrum.spectrum.split(' ').map(_.split(':').map(_.toDouble))

    val numPeaks: Integer = ions.length
    val numPeaksTransformed: Double = math.sqrt(numPeaks.toDouble)

    val meanIntensity: Double = ions.map(_(1)).sum / ions.length
    val meanIntensityTransformed: Double = math.log(meanIntensity)

    val stdIntensityTransformed: Double = math.log(math.sqrt(ions.map(x => math.pow(x(1) - meanIntensity, 2)).sum / numPeaks))



    spectrum
  }

  def findSmallestMZRange(ions: Array[Array[Double]], percentage: Double): (Double, Double) = {
    val startMZ: Double = ions.head(0)
    val endMZ: Double = ions.last(0)

    val intensitySum: Array[Double] = ions.map(_(1)).scanLeft(0.0)(_ + _)
    val totalIntensity: Double = intensitySum.last

    (0.0, 1.0)
  }
}
