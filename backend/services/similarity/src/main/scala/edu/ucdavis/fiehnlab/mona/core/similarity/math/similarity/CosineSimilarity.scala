package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

/**
  * Created by singh on 1/28/2016.
  */
class CosineSimilarity extends Similarity {

  /**
    * Computes the cosine similarity between two mass spectra
    *
    * https://www.sciencedirect.com/science/article/pii/1044030594870098
    *
    * @param unknown
    * @param reference
    * @return
    */
  def compute(unknown: SimpleSpectrum, reference: SimpleSpectrum): Double = {
    val sharedIons: Set[Double] = unknown.fragments.keySet intersect reference.fragments.keySet

    compute(unknown, reference, sharedIons)
  }

  /**
    *
    * @param unknown
    * @param reference
    * @param sharedIons
    * @return
    */
  def compute(unknown: SimpleSpectrum, reference: SimpleSpectrum, sharedIons: Set[Double]): Double = {

    val product: Double = sharedIons.toArray.map(k => reference.fragments(k).intensity * unknown.fragments(k).intensity).sum

    math.pow(product / reference.norm / unknown.norm, 2)
  }
}
