package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import edu.ucdavis.fiehnlab.mona.core.similarity.types.{Ion, SimpleSpectrum}

/**
  * Created by singh on 1/28/2016.
  */
class AbsoluteValueSimilarity extends Similarity {

  /**
    * Computes the absolute value similarity between two mass spectra
    *
    * http://ac.els-cdn.com/1044030594870098/1-s2.0-1044030594870098-main.pdf?_tid=143d0622-c54d-11e5-af32-00000aab0f26&acdnat=1453937234_932c2334be5baa8bddfb4d7fe64e5580
    *
    * @param unknown
    * @param reference
    * @return
    */
  def compute(unknown: SimpleSpectrum, reference: SimpleSpectrum): Double = {

    val zeroIon: Ion = Ion(0.0, 0.0)
    val norm = unknown.ions.map(_.intensity).sum

    val diff = reference.fragments.keySet
      .map(k => math.abs(reference.fragments(k).intensity - unknown.fragments.getOrElse(k, zeroIon).intensity))
      .sum

    math.pow(1 + diff / norm, -1)
  }
}
