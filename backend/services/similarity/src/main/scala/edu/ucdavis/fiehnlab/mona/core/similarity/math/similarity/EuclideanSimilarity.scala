package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import edu.ucdavis.fiehnlab.mona.core.similarity.types.{Ion, SimpleSpectrum}

/**
  * Created by sajjan on 10/11/16.
  */
class EuclideanSimilarity extends Similarity {

  /**
    * Computes the Euclidean similarity between two mass spectra
    *
    * https://www.sciencedirect.com/science/article/pii/1044030594870098
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
