package edu.ucdavis.fiehnlab.mona.core.similarity.types

/**
  * Created by sajjan on 12/14/16.
  */
object AlgorithmTypes extends Enumeration {
  type AlgorithmType = Value
  val ABSOLUTE_VALUE_SIMILARITY, COMPOSITE_SIMILARITY, COSINE_SIMILARITY, EUCLIDEAN_DISTANCE_SIMILARITY, DEFAULT = Value
}
