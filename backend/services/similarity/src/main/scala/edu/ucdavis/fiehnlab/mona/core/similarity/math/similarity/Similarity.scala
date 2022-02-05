package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

/**
  * Created by sajjan on 10/11/16.
  */
trait Similarity extends SimilarityComputation with LazyLogging {

  /**
    * Computes the similarity between two mass spectra with an option to remove precursor ion mass
    *
    * @param unknown
    * @param reference
    * @return
    */
  def compute(unknown: SimpleSpectrum, reference: SimpleSpectrum, removePrecursorIon: Boolean = false): Double


  /**
    * Computes the similarity between two SPLASH histogram blocks
    * TODO implement
    *
    * @param unknown
    * @param reference
    * @return
    */
  def compute(unknown: String, reference: String, removePrecursorIon: Boolean): Double = 0.0
}

