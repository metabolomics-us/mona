package edu.ucdavis.fiehnlab.mona.core.similarity.controller

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.core.similarity.service.SimilaritySearchService
import edu.ucdavis.fiehnlab.mona.core.similarity.types._
import edu.ucdavis.fiehnlab.mona.core.similarity.types.AlgorithmTypes.AlgorithmType
import edu.ucdavis.fiehnlab.mona.core.similarity.types.IndexType.IndexType
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._

import scala.collection.Set

/**
  * Created by sajjan on 1/3/17.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/similarity"))
class SimilarityController {

  @Autowired
  val indexUtilities: IndexUtils = null

  @Autowired
  val similaritySearchService: SimilaritySearchService = null


  /**
    * Main similarity search endpoint
    * @param message
    * @param size
    * @return
    */
  @RequestMapping(path = Array("/search"), method = Array(RequestMethod.POST))
  @ResponseBody
  def similaritySearch(@RequestBody message: SimilaritySearchRequest, @RequestParam(value = "size", required = false) size: Integer): Array[SearchResult] = {

    // Set search size to a value between 1 and 50, or 25 by default
    val searchSize: Int = if (size != null) Math.max(1, Math.min(size, 50)) else 25

    // Perform similarity search, order by score and return a maximum of 50 or a default 25 hits
    similaritySearchService.search(message, searchSize)
  }

  /**
    * Peak search endpoint
    * @param message
    * @param size
    * @return
    */
  @RequestMapping(path = Array("/peakSearch"), method = Array(RequestMethod.POST))
  @ResponseBody
  def peakSearch(@RequestBody message: PeakSearchRequest, @RequestParam(value = "size", required = false) size: Integer): Array[SearchResult] = {

    // Set search size to a value between 1 and 50, or 25 by default
    val searchSize: Int = if (size != null) Math.max(1, Math.min(size, 50)) else 25

    similaritySearchService.peakSearch(message, searchSize)
  }

  /**
    * Endpoint to display a list of available search indices
    * @return
    */
  @RequestMapping(path = Array("/indices"), method = Array(RequestMethod.GET))
  def getIndices: Set[IndexSummary] = {
    val indexTypes: Set[IndexType] = indexUtilities.getIndexTypes

    indexTypes.flatMap(indexType => indexUtilities.getIndexNames(indexType)
      .map(indexName => IndexSummary(indexType, indexName, indexUtilities.getIndexSize(indexName, indexType))))
  }

  /**
    * Endpoint to display a list of available search algorithms
    * @return
    */
  @RequestMapping(path = Array("/algorithms"), method = Array(RequestMethod.GET))
  def getAlgorithms: Set[AlgorithmType] = AlgorithmTypes.values
}

