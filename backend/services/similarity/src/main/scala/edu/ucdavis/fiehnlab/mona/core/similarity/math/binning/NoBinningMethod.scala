package edu.ucdavis.fiehnlab.mona.core.similarity.math.binning

import edu.ucdavis.fiehnlab.mona.core.similarity.types.{Ion, SimpleSpectrum}

/**
  * Created by sajjan on 12/13/16.
  */
class NoBinningMethod extends BinningMethod {

  override def binSpectrum(spectrum: SimpleSpectrum): SimpleSpectrum = spectrum

  override def binIon(ion: Ion): Ion = ion
}
