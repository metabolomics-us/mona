package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import edu.ucdavis.fiehnlab.mona.core.similarity.types.{Ion, SimpleSpectrum}

/**
  * Created by sajjan on 10/11/16.
  */
class EuclideanSimilarity extends Similarity {

  /**
    * Computes the Euclidean similarity between two mass spectra
    *
    * http://ac.els-cdn.com/1044030594870098/1-s2.0-1044030594870098-main.pdf?_tid=143d0622-c54d-11e5-af32-00000aab0f26&acdnat=1453937234_932c2334be5baa8bddfb4d7fe64e5580
    *
    * @param unknown
    * @param reference
    * @return
    */
  def compute(unknown: SimpleSpectrum, reference: SimpleSpectrum): Double = {

    val zeroIon: Ion = Ion(0.0, 0.0)

    val diff = reference.fragments.keySet
      .map(k => math.pow(reference.fragments(k).intensity - unknown.fragments.getOrElse(k, zeroIon).intensity, 2))
      .sum

    math.pow(1 + diff / unknown.norm / unknown.norm, -1)
  }
}
