package edu.ucdavis.fiehnlab.mona.core.similarity.math.binning

import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

/**
  * Created by sajjan on 12/13/16.
  */
class NoBinningMethod extends BinningMethod {
  def binSpectrum(spectrum: SimpleSpectrum): SimpleSpectrum = spectrum
}
