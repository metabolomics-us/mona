package edu.ucdavis.fiehnlab.mona.core.similarity.controller

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
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
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val indexUtilities: IndexUtils = null


  @RequestMapping(path = Array("/search"), method = Array(RequestMethod.POST))
  @ResponseBody
  def similaritySearch(@RequestBody message: SimilaritySearchRequest): Array[SearchResult] = {

    val spectrum: SimpleSpectrum = new SimpleSpectrum(null, message.spectrum)

    indexUtilities.search(spectrum, AlgorithmTypes.DEFAULT, 0.5)
      .toArray
      .sortBy(-_.score)
      .take(10)
      .map(x => SearchResult(spectrumMongoRepository.findOne(x.hit.id), x.score))
  }

  @RequestMapping(path = Array("/peakSearch"), method = Array(RequestMethod.GET))
  @ResponseBody
  def peakSearch(): Array[Spectrum] = {
    Array()
  }

  @RequestMapping(path = Array("/indices"), method = Array(RequestMethod.GET))
  def getIndices: Set[IndexSummary] = {
    val indexTypes: Set[IndexType] = indexUtilities.getIndexTypes

    indexTypes.flatMap(indexType => indexUtilities.getIndexNames(indexType)
      .map(indexName => IndexSummary(indexType, indexName, indexUtilities.getIndexSize(indexType, indexName))))
  }

  @RequestMapping(path = Array("/algorithms"), method = Array(RequestMethod.GET))
  def getAlgorithms: Set[AlgorithmType] = AlgorithmTypes.values
}
