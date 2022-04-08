package edu.ucdavis.fiehnlab.mona.core.similarity.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.core.similarity.types._
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 2/28/17.
  */
@Service
class SimilaritySearchService extends LazyLogging {

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val indexUtils: IndexUtils = null

  /**
    *
    * @param request
    * @return
    */
  def search(request: SimilaritySearchRequest, size: Int): Array[SearchResult] = {
    val spectrum: SimpleSpectrum =
    if (request.removePrecursorIon == false && request.precursorMZ == 0.0) {
      logger.info(s"remove Precursor False and No PrecursorMZ")
      new SimpleSpectrum(null, request.spectrum)
    } else {
      new SimpleSpectrum(null, request.spectrum, request.precursorMZ)
    }


    val minSimilarity: Double =
      if (request.minSimilarity > 0.0 && request.minSimilarity <= 1.0) {
        request.minSimilarity
      } else if (request.minSimilarity > 100 && request.minSimilarity <= 1000) {
        request.minSimilarity / 1000
      } else {
        0.5
      }

    logger.info(s"Starting similarity search with minimum similarity $minSimilarity")

    val algorithmType: AlgorithmTypes.Value = getSimilarityType(request.algorithm)
    // Perform similarity search, order by score and return a maximum of 50 or a default 25 hits
    val results: Array[ComputationalResult] = indexUtils.search(spectrum, algorithmType, minSimilarity, request.precursorToleranceDa, request.removePrecursorIon).toArray
    logger.info(s"Search discovered ${results.length} hits")

    results
      // Filter by tags
      .filter(x => request.requiredTags == null || request.requiredTags.forall(t => x.hit.tags.contains(t)))
      .filter(x => request.filterTags == null || request.filterTags.isEmpty || request.filterTags.exists(t => x.hit.tags.contains(t)))

      // Sort by score and return SearchResult objects
      .sortBy(-_.score)
      .take(size)
      .map(x => SearchResult(spectrumMongoRepository.findOne(x.hit.id), x.score))
  }

  /**
    *
    * @param request
    * @return
    */
  def peakSearch(request: PeakSearchRequest, size: Int): Array[SearchResult] = {
    val searchTolerance: Double = if (request.tolerance > 0) request.tolerance else 1

    indexUtils.getIndex("default", IndexType.PEAK).get(request.peaks)
      .view
      .filter(spectrum => request.peaks.forall(mz => spectrum.ions.exists(ion => Math.abs(ion.mz - mz) <= searchTolerance)))
      .take(size)
      .map(x => SearchResult(spectrumMongoRepository.findOne(x.id), 1))
      .toArray
  }

  def getSimilarityType(algorithm: String): AlgorithmTypes.Value = {
    val algType = algorithm match {
      case "absolute" => AlgorithmTypes.ABSOLUTE_VALUE_SIMILARITY
      case "composite" => AlgorithmTypes.COMPOSITE_SIMILARITY
      case "cosine" => AlgorithmTypes.COSINE_SIMILARITY
      case "entropy" => AlgorithmTypes.ENTROPY_SIMILARITY
      case "euclidean" => AlgorithmTypes.EUCLIDEAN_DISTANCE_SIMILARITY
      case "default" => AlgorithmTypes.DEFAULT
      case _ => AlgorithmTypes.DEFAULT
    }
    algType
  }
}
