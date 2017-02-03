package edu.ucdavis.fiehnlab.mona.core.similarity.types

import edu.ucdavis.fiehnlab.mona.core.similarity.types.IndexType.IndexType

/**
  * Created by sajjan on 1/4/17.
  */
case class IndexSummary(indexType: IndexType, indexName: String, indexSize: Int)

case class IndexSummaryRequest()

case class SimilaritySearchRequest(spectrum: String)