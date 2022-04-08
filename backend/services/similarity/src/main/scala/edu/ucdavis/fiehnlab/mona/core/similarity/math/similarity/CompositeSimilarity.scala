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
      val newUnknown: SimpleSpectrum = removePrecursor(unknown, unknown.precursorMZ)
      val newReference: SimpleSpectrum = removePrecursor(reference, unknown.precursorMZ)
      doCompute(newUnknown, newReference)
    } else {
      doCompute(unknown, reference)
    }
  }
}
