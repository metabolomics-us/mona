package edu.ucdavis.fiehnlab.mona.core.similarity.math.binning

import edu.ucdavis.fiehnlab.mona.core.similarity.types.{BinnedSimpleSpectrum, Ion, SimpleSpectrum}
import edu.ucdavis.fiehnlab.mona.core.similarity.util.SpectrumUtils

import scala.collection.mutable

/**
  * Created by sajjan on 12/13/16.
  */
class BinByRoundingMethod extends BinningMethod {

  /**
    * converts the given spectrum into a binned spectrum
    *
    * @param spectrum
    * @return
    */
  override def binSpectrum(spectrum: SimpleSpectrum): SimpleSpectrum = {

    // Create map to accumulate binned ions
    val binnedIons: mutable.Map[Long, Double] = mutable.Map[Long, Double]().withDefaultValue(0.0)

    spectrum.ions.foreach(ion => binnedIons(SpectrumUtils.roundMZ(ion.mz)) += ion.intensity)

    // Collect bins as ions
    val ions: Array[Ion] = binnedIons.keys.map(x => Ion(x, binnedIons(x))).toArray.sortBy(_.mz)

    new SimpleSpectrum(spectrum.id, ions, spectrum.chromatography, spectrum.public) with BinnedSimpleSpectrum
  }
}
