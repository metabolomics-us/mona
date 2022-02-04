package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum
import edu.ucdavis.fiehnlab.mona.core.similarity.types.Ion

/**
  * Created by wohlgemuth on 1/27/16.
  */
class CompositeSimilarity extends Similarity {

  /**
   * Computes the composite similarity between two mass spectra
   *
   *
   * @param unknown
   * @param reference
   * @return
   */
  override final def compute(unknown: SimpleSpectrum, reference: SimpleSpectrum, removePrecursorIon: Boolean): Double = {
    if(removePrecursorIon) {
      doCompute(removePrecursor(unknown), removePrecursor(reference))
    } else {
      doCompute(unknown, reference)
    }
  }

  //Removes precursor ion if removePrecursorIon flag is set else returns spectrum as is
  protected def removePrecursor(spectrum: SimpleSpectrum): SimpleSpectrum = {
    val precursorRemovedIons: Array[Ion] = spectrum.ions.filter(_.mz < spectrum.precursorMZ - (5 / 1000))

    new SimpleSpectrum(spectrum.id, precursorRemovedIons, spectrum.precursorMZ, spectrum.tags, spectrum.public)
  }
}
