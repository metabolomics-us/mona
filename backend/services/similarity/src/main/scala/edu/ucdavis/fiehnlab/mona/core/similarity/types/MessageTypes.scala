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
                                    precursorTolerancePPM: Double
                                  )

object SimilaritySearchRequest {
  def apply(spectrum: String, minSimilarity: Double): SimilaritySearchRequest = SimilaritySearchRequest(spectrum, minSimilarity, 0.0, 0.0, 0.0)
}


case class PeakSearchRequest(peaks: Array[Double], tolerance: Double)