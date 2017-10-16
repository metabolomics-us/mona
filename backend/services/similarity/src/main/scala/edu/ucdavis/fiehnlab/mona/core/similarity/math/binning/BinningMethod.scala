package edu.ucdavis.fiehnlab.mona.core.similarity.math.binning

import edu.ucdavis.fiehnlab.mona.core.similarity.types.{Ion, SimpleSpectrum}

/**
  * Created by sajjan on 12/7/16.
  */
trait BinningMethod {

  def binSpectrum(spectrum: SimpleSpectrum): SimpleSpectrum

  def binIon(ion: Ion): Ion
}






