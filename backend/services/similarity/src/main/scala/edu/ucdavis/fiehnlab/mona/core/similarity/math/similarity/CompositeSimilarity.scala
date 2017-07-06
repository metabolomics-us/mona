package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

/**
  * Created by wohlgemuth on 1/27/16.
  */
class CompositeSimilarity extends Similarity {

  /**
    * Computes the composite similarity between two mass spectra
    *
    * https://www.sciencedirect.com/science/article/pii/1044030594870098
    *
    * @param unknown
    * @param reference
    * @return
    */
  override def compute(unknown: SimpleSpectrum, reference: SimpleSpectrum): Double = {

    val sharedIonsSet: Set[Double] = reference.fragments.keySet intersect unknown.fragments.keySet
    val sharedIons: Array[Double] = sharedIonsSet.toArray.sorted

    val cosineSimilarity = new CosineSimilarity().compute(unknown, reference, sharedIonsSet)

    if (sharedIons.length > 1) {
      // Takes the ratio of successive list elements, ie A[i] / A[i + 1]
      val unknownRatios: Iterator[Double] = sharedIons.map(k => unknown.fragments(k)).sliding(2).map { case Array(x, y) => x.intensity / y.intensity }
      val libraryRatios: Iterator[Double] = sharedIons.map(k => reference.fragments(k)).sliding(2).map { case Array(x, y) => x.intensity / y.intensity }

      // Divide the unknown ratio by the library ratio
      val combinedRatios: Iterator[Double] = unknownRatios.zip(libraryRatios).map { case (x, y) => 1.0 * x / y }

      // Ensure each term is less than 1 and then sum
      val intensitySimilarity: Double = 1 + combinedRatios.map { x => Math.min(x, 1 / x) }.sum

      // Compute the composite similarity
      (unknown.fragments.size * cosineSimilarity + intensitySimilarity) / (unknown.fragments.size + sharedIons.length)
    } else {
      cosineSimilarity
    }
  }
}
