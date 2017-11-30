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
    * processes the given spectrum
    *
    * @param spectrum to be processed
    * @return processed spectrum
    */
  override def process(spectrum: Spectrum): Spectrum = {
    // Compute the descriptive features defined in Nesvizhskii et al (2006):
    // http://www.mcponline.org/content/5/4/652.full
    val ions: Array[Array[Double]] = spectrum.spectrum.split(' ').map(_.split(':').map(_.toDouble))

    // Calculate spectrum features
    val numPeaksTransformed: Double = math.sqrt(ions.length)

    val meanIntensityTransformed: Double = math.log(ions.map(_ (1)).sum / ions.length)

    val stdIntensityTransformed: Double = math.log(standardDeviation(ions.map(_ (1))))

    val smallestMZRange95: Double = findSmallestMZRange(ions, 0.95)
    val smallestMZRange50: Double = findSmallestMZRange(ions, 0.50)

    val totalIonCurrentTranformed: Double = math.log(ions.map(_ (1)).sum / smallestMZRange95)

    val mzDifferencesTransformed: Double = math.log(standardDeviation(ions.map(_ (0)).sorted.sliding(2).map(x => x(1) - x(0)).toArray))

    val meanNeighborsIn2Da: Double = ions.map(x => countPeaksInWindow(ions, x(0), 2)).sum / ions.length


    spectrum
  }

  /**
    * Calculates the standard deviation of an array of values
    *
    * @param values
    * @return
    */
  def standardDeviation(values: Array[Double]): Double = {
    val mean: Double = values.sum / values.length

    math.sqrt(values.map(x => (x - mean) * (x - mean)).sum / values.length)
  }

  /**
    * Returns the width of the smallest m/z range that contains at least a given
    * percentage of the total intensity of the mass spectrum
    *
    * @param ions
    * @param percentage
    * @return
    */
  def findSmallestMZRange(ions: Array[Array[Double]], percentage: Double): Double = {
    // Sort the ions by m/z
    val sortedIons: Array[Array[Double]] = ions.sortBy(_ (0))
    val mz: Array[Double] = sortedIons.map(_ (0))

    // Compute an accumulation of the intensity values, so that the intensitySum.head
    // is 0 and intensitySum.last is the total summed intensity of the spectrum
    val intensitySum: Array[Double] = sortedIons.map(_ (1)).scanLeft(0.0)(_ + _)
    val totalIntensity: Double = intensitySum.last

    // Find all pairs of indices whose intensity sum meets the minimum criteria
    // and translate the result to the m/z difference
    val ranges: List[Double] = intensitySum.indices.combinations(2)
      .filter(x => intensitySum(x(1)) - intensitySum(x(0)) >= percentage * totalIntensity)
      .map(x => mz(x(1) - 1) - mz(x(0)) + 1.0).toList

    // Return the smallest m/z range
    if (ranges.nonEmpty)
      ranges.min
    else
      mz.last - mz.head
  }

  def countPeaksInWindow(ions: Array[Array[Double]], mz: Double, tolerance: Double): Int = {
    ions.count(x => math.abs(x(0) - mz) < tolerance)
  }
}
