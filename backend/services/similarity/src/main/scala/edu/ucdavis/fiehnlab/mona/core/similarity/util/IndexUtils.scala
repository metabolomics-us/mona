package edu.ucdavis.fiehnlab.mona.core.similarity.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.core.similarity.index.{Index, IndexRegistry}
import edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity._
import edu.ucdavis.fiehnlab.mona.core.similarity.types.AlgorithmTypes.AlgorithmType
import edu.ucdavis.fiehnlab.mona.core.similarity.types.IndexType.IndexType
import edu.ucdavis.fiehnlab.mona.core.similarity.types._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.Set


/**
  * Created by sajjan on 1/2/17.
  */
@Component
class IndexUtils extends LazyLogging {

  @Autowired
  private val indexRegistry: IndexRegistry = null


  /**
    *
    * @param indexName
    * @param indexType
    * @return
    */
  def getIndex(indexName: String, indexType: IndexType): Index = indexRegistry.getIndex(indexType, indexName)

  /**
    *
    * @param spectrum
    * @param indexName
    * @param indexType
    * @return
    */
  def addToIndex(spectrum: SimpleSpectrum, indexName: String, indexType: IndexType): Int = {

    val index: Index = indexRegistry.getIndex(indexType, indexName)

    logger.debug(s"adding spectrum ${spectrum.id} to index: $indexName - $indexType")
    index.index(spectrum)

    index.size
  }

  /**
    * Add spectrum to the default index
    *
    * @param spectrum
    * @return
    */
  def addToIndex(spectrum: SimpleSpectrum): Int = addToIndex(spectrum, null, null)

  /**
    * Access to the internal indexRegistry
    *
    * @return
    */
  def getIndexTypes: Set[IndexType] = indexRegistry.getTypes

  /**
    * set of names
    *
    * @param indexType
    * @return
    */
  def getIndexNames(indexType: IndexType): Set[String] = indexRegistry.getNames(indexType)

  /**
    * Size of a given index
    *
    * @param indexType
    * @param indexName
    * @return
    */
  def getIndexSize(indexName: String, indexType: IndexType): Int = {
    logger.info(s"Requesting size for index: $indexName - $indexType")
    indexRegistry.getIndex(indexType, indexName).size
  }

  /**
    * Get size of the default index
    *
    * @return
    */
  def getIndexSize: Int = getIndexSize(null, null)


  /**
    * Perform a blocking search
    *
    * @param spectrum
    * @param indexName
    * @param indexType
    * @param algorithm
    * @param threshold
    * @return
    */
  def search(spectrum: SimpleSpectrum, indexName: String, indexType: IndexType, algorithm: AlgorithmType, threshold: Double, precursorToleranceDa: Double, removePrecursorIon: Boolean): Iterable[ComputationalResult] = {

    // Instantiate the specified similarity algorithm
    val similarityAlgorithm: Similarity = algorithm match {
      case AlgorithmTypes.ABSOLUTE_VALUE_SIMILARITY =>
        new AbsoluteValueSimilarity

      case AlgorithmTypes.EUCLIDEAN_DISTANCE_SIMILARITY =>
        new EuclideanSimilarity

      case AlgorithmTypes.COSINE_SIMILARITY =>
        new CosineSimilarity

      case AlgorithmTypes.COMPOSITE_SIMILARITY =>
        new CompositeSimilarity

      case AlgorithmTypes.ENTROPY_SIMILARITY =>
        new EntropySimilarity

      case AlgorithmTypes.DEFAULT =>
        new EntropySimilarity

      case _ =>
        new EntropySimilarity
    }

    logger.info(s"Index $indexType $indexName, algorithm $algorithm")

    // Get the specified index and perform a search
    indexRegistry.getIndex(indexType, indexName).search(spectrum, similarityAlgorithm, threshold, precursorToleranceDa, removePrecursorIon)
  }

  /**
    * Perform a blocking search in the default index
    *
    * @param spectrum
    * @return
    */
  def search(spectrum: SimpleSpectrum, algorithm: AlgorithmType, threshold: Double, precursorToleranceDa: Double, removePrecursorIon: Boolean): Iterable[ComputationalResult] =
    search(spectrum, null, null, algorithm, threshold, precursorToleranceDa, removePrecursorIon)
}
