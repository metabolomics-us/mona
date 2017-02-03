package edu.ucdavis.fiehnlab.mona.core.similarity.math.histogram

import edu.ucdavis.fiehnlab.mona.core.similarity.types.Ion


/**
  * Created by sajjan on 2/17/16.
  *
  * @param base numerical range of each digit in the histogram
  * @param length  how many characters long the histogram shall be
  * @param binSize size of the bins in nominal masses
  *
  */
class SplashHistogram(val base: Int, val length: Int, val binSize: Int) extends Histogram {
  val EPS_CORRECTION = 1.0e-7

  /**
    * Generates a histogram based of the given fragments
    *
    * @param ions
    * @return
    */
  def generate(ions: Array[Ion]): String = {

    // Create an array for binning the ions
    val binnedIons: Array[Double] = Array.fill(length)(0.0)

    // Bin ions using the histogram wrapping strategy
    ions.foreach { ion: Ion =>
      val bin: Int = (ion.mz / binSize).toInt % length
      binnedIons(bin) = binnedIons(bin) + ion.intensity
    }

    // Normalize the histogram and scale to the provided base
    val maxIntensity: Double = binnedIons.max
    binnedIons.transform(bin => (base - 1) * bin / maxIntensity)

    // Build histogram string
    // Apply a small correction to account for floating point precision errors
    binnedIons.map(bin => BigInt((EPS_CORRECTION + bin).toInt).toString(base)).mkString
  }

  override def toString: String = this.getClass.getSimpleName + "(" + base + "/" + binSize + "/" + length + ")"
}

object SplashHistogram {
  // Histogram used by the SPLASH algorithm
  def create(): SplashHistogram = new SplashHistogram(10, 10, 100)
}