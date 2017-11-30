package edu.ucdavis.fiehnlab.mona.core.similarity.util

import edu.ucdavis.fiehnlab.mona.core.similarity.types.{Ion, SimpleSpectrum}
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil

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
    var ions: Array[Ion] = spectrum
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
}
