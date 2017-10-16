package edu.ucdavis.fiehnlab.mona.core.similarity.math.histogram

import edu.ucdavis.fiehnlab.mona.core.similarity.types.{Ion, SimpleSpectrum}

/**
  * Created by sajjan on 2/17/16.
  */
trait Histogram {

  /**
    * Generates a histogram based of the given fragments
    *
    * @param ions
    * @return
    */
  def generate(ions: Array[Ion]): String

  /**
    * Generates a histogram based on the given spectrum
    * @param spectrum
    * @return
    */
  def generate(spectrum: SimpleSpectrum): String = generate(spectrum.ions)
}

object Histogram {
  def create(): Histogram = SplashHistogram.create()
}