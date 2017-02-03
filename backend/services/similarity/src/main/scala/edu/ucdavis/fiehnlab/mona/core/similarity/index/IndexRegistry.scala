package edu.ucdavis.fiehnlab.mona.core.similarity.index

import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.core.similarity.index.cache.SpectrumCache
import edu.ucdavis.fiehnlab.mona.core.similarity.math.binning.BinningMethod
import edu.ucdavis.fiehnlab.mona.core.similarity.math.histogram.SplashHistogram
import edu.ucdavis.fiehnlab.mona.core.similarity.types.IndexType
import edu.ucdavis.fiehnlab.mona.core.similarity.types.IndexType.IndexType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection._
import scala.collection.concurrent.TrieMap

/**
  * Created by sajjan on 12/14/16.
  */
@Component
class IndexRegistry extends LazyLogging {

  @Autowired
  private val binningMethod: BinningMethod = null

  @Autowired
  private val spectrumCache: SpectrumCache = null


  private val indices: concurrent.Map[IndexType, mutable.Map[String, Index]] = new TrieMap[IndexType, mutable.Map[String, Index]]()


  @PostConstruct
  def init(): Unit = {
    logger.info("configure default indexes")

    getIndex(IndexType.HISTOGRAM, "default")
    getIndex(IndexType.SIMILAR_HISTOGRAM, "default")
    getIndex(IndexType.DEFAULT, "default")
  }


  /**
    * Gets all indexes with the specified name
    *
    * @param name
    * @return
    */
  def getIndexesByName(name: String): Set[Index] = {
    indices
      .filter(_._2 != null)
      .map(_._2.getOrElse(name, null))
      .filter(_ != null)
      .toSet
  }

  /**
    * provides us with the requested index
    *
    * @param indexType
    * @param name
    * @return
    */
  def getIndex(indexType: IndexType, name: String): Index = {

    if (indexType == null) {
      getIndex(IndexType.DEFAULT, name)
    }

    else if (name == null || name.isEmpty) {
      getIndex(indexType, "default")
    }

    else {
      if (!indices.contains(indexType)) {
        indices.put(indexType, mutable.Map[String, Index]())
      }

      val names: mutable.Map[String, Index] = indices(indexType)

      if (!names.contains(name)) {
        logger.info(s"registering new index with name $name and of type $indexType")

        indexType match {
          case IndexType.HISTOGRAM =>
            names.put(name, new HistogramIndex(binningMethod, spectrumCache, SplashHistogram.create()))

          case IndexType.SIMILAR_HISTOGRAM =>
            names.put(name, new SimilarHistogramIndex(binningMethod, spectrumCache, SplashHistogram.create(), 0.8))

          case IndexType.DEFAULT =>
            names.put(name, new SimilarHistogramIndex(binningMethod, spectrumCache, SplashHistogram.create(), 0.8))

          case _ =>
            throw new RuntimeException("Unable to create the requested index")
        }
      }

      names(name)
    }
  }

  /**
    * Returns all registered index types
    *
    * @return
    */
  def getTypes: Set[IndexType] = indices.keySet

  /**
    * Return all registered names for the given index type
    *
    * @param indexType
    * @return
    */
  def getNames(indexType: IndexType): Set[String] = indices(indexType).keySet
}
