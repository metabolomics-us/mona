package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

/**
  * Created by singh on 1/28/2016.
  */
class CosineSimilarity extends Similarity {

  /**
    * Computes the cosine similarity between two mass spectra
    *
    * http://ac.els-cdn.com/1044030594870098/1-s2.0-1044030594870098-main.pdf?_tid=143d0622-c54d-11e5-af32-00000aab0f26&acdnat=1453937234_932c2334be5baa8bddfb4d7fe64e5580
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

    product / reference.norm / unknown.norm
  }
}
