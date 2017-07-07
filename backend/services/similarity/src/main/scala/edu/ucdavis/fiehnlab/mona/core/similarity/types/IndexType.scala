package edu.ucdavis.fiehnlab.mona.core.similarity.types

/**
  * Created by sajjan on 12/14/16.
  */
object IndexType extends Enumeration {
  type IndexType = Value
  val HISTOGRAM, SIMILAR_HISTOGRAM, PEAK, DEFAULT = Value
}