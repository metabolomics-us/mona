package edu.ucdavis.fiehnlab.mona.core.similarity.util

import edu.ucdavis.fiehnlab.mona.core.similarity.types.{Ion, SimpleSpectrum}
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil

import scala.util.control.Breaks.{break, breakable}

/**
  * Created by sajjan on 10/11/16.
  */
object SpectrumUtils {

  val EPS_CORRECTION: Double = 0.2

  val NOMINAL_SEARCH_TOL: Double = 0.5
  val ACCURATE_SEARCH_TOL: Double = 0.05


  /**
    * Rounds m/z values based on the 80/20 rule
    *
    * @param ion
    * @return
    */
  def roundMZ(ion: Ion): Ion = ion.copy(mz = roundMZ(ion.mz))


  /**
    * Rounds m/z values based on the 80/20 rule
    *
    * @param mz
    * @return
    */
  def roundMZ(mz: Double): Int = scala.math.floor(mz + EPS_CORRECTION).toInt


  /**
    * Find all ions within the search accuracy window and select ion with the highest intensity
    *
    * @param selectedIon
    * @param referenceSpectrum
    * @param accurateSearch
    * @return
    */
  def findIonMatch(selectedIon: Ion, referenceSpectrum: SimpleSpectrum, accurateSearch: Boolean): Ion = {

    val tolerance = if (accurateSearch) ACCURATE_SEARCH_TOL else NOMINAL_SEARCH_TOL

    referenceSpectrum.ions
      .filter(x => Math.abs(x.mz - selectedIon.mz) < tolerance)
      .maxBy(_.intensity)
  }

  /**
    *
    * @param ions
    * @return
    */
  def ionsToString(ions: Iterable[Ion]): String = ions.mkString(" ")

  /**
    *
    * @param spectrum
    * @param normalizeSpectrum
    * @return
    */
  def stringToIons(spectrum: String, normalizeSpectrum: Boolean = false): Array[Ion] = {
    if (spectrum.isEmpty) {
      Array()
    } else {
      val ions: Array[Ion] = spectrum
        .split(' ')
        .map(_.split(':'))
        .map(ion => Ion(ion(0).toDouble, ion(1).toDouble))
        .sortBy(_.mz)

      if (normalizeSpectrum) {
        val maxIntensity: Double = ions.map(_.intensity).max
        ions.map(ion => ion.copy(intensity = 100 * ion.intensity / maxIntensity))
      } else {
        ions
      }
    }
  }


  /**
    * SPLASH the given spectrum
    *
    * @param spectrum
    * @return
    */
  def splashSpectrum(spectrum: SimpleSpectrum): String = splashIons(spectrum.ions)

  /**
    * SPLASH the given collection of ions
    *
    * @param ions
    * @return
    */
  def splashIons(ions: Iterable[Ion]): String = SplashUtil.splash(ionsToString(ions), SpectraType.MS)


  /**
    * Calculates the Manhattan similarity between 2 histograms
    * TODO: Replace with more generalized similarity approach``
    *
    * @param hist1
    * @param hist2
    * @param base
    * @return
    */
  def calculateHistogramSimilarity(hist1: String, hist2: String, base: Int): Double = {
    val unknownHistogram: Seq[Int] = hist1.map { c => Integer.parseInt(c.toString, base) }
    val libraryHistogram: Seq[Int] = hist2.map { c => Integer.parseInt(c.toString, base) }

    // zipAll fills the shorter histogram to the length of the longer with zeros
    val zippedHistogram = unknownHistogram.zipAll(libraryHistogram, 0, 0)

    // Compute the manhattan similarity
    1 - zippedHistogram.map { case (x, y) => Math.abs(x - y) }.sum.toDouble / (unknownHistogram.sum + libraryHistogram.sum)
  }

  def clean_spectrum(spec: Array[Array[Double]], ms2_da: Double = -1, ms2_ppm: Double = -1): Array[Array[Double]] = {
    var intensity_sum: Double = 0
    for (peak <- spec) {
      intensity_sum += peak(1)
    }
    // Add non-zero peak and normalize it.
    var spec_new: Array[Array[Double]] = Array()
    for (peak <- spec) {
      if (peak(1) > 0.0) {
        peak(1) /= intensity_sum
        spec_new = spec_new :+ peak
      }
    }
    // Centroid the spectrum
    spec_new = centroid_spectrum(spec_new, ms2_da, ms2_ppm)
    spec_new
  }

  def centroid_spectrum(spec_ori: Array[Array[Double]], ms2_da: Double, ms2_ppm: Double): Array[Array[Double]] = {
    // Sort the spectrum by m/z
    var spec = spec_ori.map(_.clone)
    spec = spec.sortBy(x => x(0))
    // Get intensity order
    var intensity_order: Array[Array[Double]] = Array()
    var i = 0
    for (peak <- spec) {
      intensity_order = intensity_order :+ Array(i, 0.0 - peak(1))
      i += 1
    }
    intensity_order = intensity_order.sortBy(x => x(1))
    var spec_new: Array[Array[Double]] = Array()
    for (i_order <- intensity_order) {
      val i = i_order(0).toInt
      var mz_delta_allowed: Double = ms2_da
      if (mz_delta_allowed < 0) {
        mz_delta_allowed = ms2_ppm * 1e-6 * spec(i)(0)
      }
      if (spec(i)(1) > 0) {
        // Find left board for current peak
        var i_left = i - 1
        breakable {
          while (i_left >= 0) {
            var mz_delta_left = spec(i)(0) - spec(i_left)(0)
            if (mz_delta_left <= mz_delta_allowed) {
              i_left -= 1
            } else {
              break
            }
          }
        }
        i_left += 1
        //Find right board for current peak
        var i_right = i + 1
        breakable {
          while (i_right < spec.length) {
            val mz_delta_right = spec(i_right)(0) - spec(i)(0)
            if (mz_delta_right <= mz_delta_allowed) {
              i_right += 1
            } else {
              break
            }
          }
        }
        // Merge those peaks
        var intensity_sum: Double = 0
        var intensity_weighted_sum: Double = 0
        for (j <- i_left until i_right) {
          intensity_sum += spec(j)(1)
          intensity_weighted_sum += spec(j)(0) * spec(j)(1)
        }
        spec_new = spec_new :+ Array(intensity_weighted_sum / intensity_sum, intensity_sum)
        for (j <- i_left until i_right) {
          spec(j)(1) = 0
        }
      }
    }
    // Sort again the spectrum by m/z
    spec_new.sortBy(x => x(0))
  }

  def toIonSeq(spec: String): Seq[Ion] = {
    spec.trim.split("\\s").collect {
      case ion: String if ion.nonEmpty =>
        val peak = ion.split(":")
        Ion(peak(0).toDouble, peak(1).toFloat)
    }.toSeq
  }

  def toArray(spec: String): Array[Array[Double]] = {
    spec.trim.split(" ").collect {
      case ion: String if ion.nonEmpty =>
        val peak = ion.split(":")
        Array(peak(0).toDouble, peak(1).toDouble)
    }
  }

  def toArray(spectrum: Seq[Ion]): Array[Array[Double]] = {
    spectrum.map { ion: Ion => Array(ion.mz, ion.intensity) }.toArray
  }
}
