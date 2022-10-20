package edu.ucdavis.fiehnlab.mona.core.similarity.types

import edu.ucdavis.fiehnlab.mona.core.similarity.types.IndexType.IndexType

/**
  * Created by sajjan on 1/4/17.
  */
case class IndexSummary(indexType: IndexType, indexName: String, indexSize: Int)

case class IndexSummaryRequest()

case class SimilaritySearchRequest(
                                    spectrum: String,
                                    minSimilarity: Double,
                                    precursorMZ: Double,
                                    precursorToleranceDa: Double,
                                    precursorTolerancePPM: Double,
                                    requiredTags: Array[String],
                                    filterTags: Array[String],
                                    removePrecursorIon: Boolean,
                                    algorithm: String,
                                    checkAllAdducts: Boolean
                                  )

object SimilaritySearchRequest {
  def apply(spectrum: String, minSimilarity: Double): SimilaritySearchRequest =
    SimilaritySearchRequest(spectrum, minSimilarity, 0.0, 0.01, 0.0, Array(), Array(), false, "default", false)

  def apply(spectrum: String, minSimilarity: Double, precursorMZ: Double, precursorToleranceDa: Double, precursorTolerancePPM: Double, removePrecursorIon: Boolean, algorithm: String, checkAllAdducts: Boolean): SimilaritySearchRequest =
    SimilaritySearchRequest(spectrum, minSimilarity, precursorMZ, precursorToleranceDa, precursorTolerancePPM, Array(), Array(), removePrecursorIon, algorithm, checkAllAdducts)
}


case class PeakSearchRequest(peaks: Array[Double], tolerance: Double)
