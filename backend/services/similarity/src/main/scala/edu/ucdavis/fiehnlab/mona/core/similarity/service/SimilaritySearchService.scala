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
    val spectrum: SimpleSpectrum = new SimpleSpectrum(null, request.spectrum)

    val minSimilarity: Double =
      if (request.minSimilarity > 0.0 && request.minSimilarity <= 1.0) {
        request.minSimilarity
      } else if (request.minSimilarity > 100 && request.minSimilarity <= 1000) {
        request.minSimilarity / 1000
      } else {
        0.5
      }

    logger.info(s"Starting similarity search with minimum similarity $minSimilarity")

    // Perform similarity search, order by score and return a maximum of 50 or a default 25 hits
    val results: Array[ComputationalResult] = indexUtils.search(spectrum, AlgorithmTypes.DEFAULT, minSimilarity).toArray


    logger.info(s"Search discovered ${results.length} hits")

    // Filter by precursor m/z if available, sort by score and return SearchResult objects
    if (request.precursorMZ > 0.0) {
      filterSimilaritySearchResults(request, results)
        .sortBy(-_.score)
        .take(size)
        .map(x => SearchResult(spectrumMongoRepository.findOne(x.hit.id), x.score))
    } else {
      results
        .sortBy(-_.score)
        .take(size)
        .map(x => SearchResult(spectrumMongoRepository.findOne(x.hit.id), x.score))
    }
  }

  /**
    *
    * @param request
    * @param results
    * @return
    */
  private def filterSimilaritySearchResults(request: SimilaritySearchRequest, results: Array[ComputationalResult]): Array[ComputationalResult] = {

    // Determine tolerance value based on precursor m/z and ppm value,
    // get provided tolerance value or use default of 0.5 Da
    val tolerance: Double =
    if (request.precursorTolerancePPM > 0.0) {
      request.precursorMZ / 1.0e6 * request.precursorTolerancePPM
    } else if (request.precursorToleranceDa > 0.0) {
      request.precursorToleranceDa
    } else {
      0.5
    }

    logger.info(s"Filtering by precursor m/z ${request.precursorMZ} with tolerance +/-$tolerance Da" +
      (if (request.precursorTolerancePPM > 0.0) s" (+/- ${request.precursorTolerancePPM} ppm)" else ""))

    results.filter(x => Math.abs(x.hit.precursorMZ - request.precursorMZ) <= tolerance)
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
}
