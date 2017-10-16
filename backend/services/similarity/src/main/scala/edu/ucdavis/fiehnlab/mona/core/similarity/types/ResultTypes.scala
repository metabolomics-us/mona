package edu.ucdavis.fiehnlab.mona.core.similarity.types

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum

/**
  * Created by sajjan on 12/28/16.
  */

/**
  *
  * @param unknown
  * @param hit
  * @param score
  */
case class ComputationalResult(unknown: SimpleSpectrum, hit: SimpleSpectrum, score: Double) extends Ordered[ComputationalResult] {

  override def compare(that: ComputationalResult): Int = this.score.compareTo(that.score)

  override def toString: String = s"${unknown.splash} vs ${hit.splash} = $score"
}

/**
  *
  * @param hit
  * @param score
  */
case class SearchResult(hit: Spectrum, score: Double) extends Ordered[SearchResult] {

  override def compare(that: SearchResult): Int = this.score.compareTo(that.score)

  override def toString: String = s"${hit.splash}: $score"
}