package edu.ucdavis.fiehnlab.mona.core.similarity.index

import edu.ucdavis.fiehnlab.mona.core.similarity.index.cache.SpectrumCache
import edu.ucdavis.fiehnlab.mona.core.similarity.math.binning.{BinningMethod, NoBinningMethod}
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

import scala.collection.JavaConversions._

/**
  * Simplest possible index
  */
class LinearIndex(override val binningMethod: BinningMethod, override val cache: SpectrumCache) extends Index(binningMethod, cache) {

  def this() = this(new NoBinningMethod, SpectrumCache.create())

  /**
    * Contains our actual data in a standard java collection
    */
  var indexMap: java.util.Map[String, SimpleSpectrum] = createBacking()

  /**
    * Defines the collection to use for the backing, allowing for easy overwriting in subclasses to
    * determine the optimal mapping for different use cases
    *
    * @return
    */
  protected def createBacking(): java.util.Map[String, SimpleSpectrum] = new java.util.concurrent.ConcurrentHashMap[String, SimpleSpectrum]()

  /**
    * Searches in the index for the given spectrum, which should be done by equality and
    * might have linear performance depending of implementation
    *
    * @param spectrum
    * @return
    */
  override def lookup(spectrum: SimpleSpectrum): Option[SimpleSpectrum] = {
    logger.trace(s"looking up: ${spectrum.splash}")

    indexMap.get(spectrum.splash) match {
      case null => None
      case x: SimpleSpectrum => Some(x)
    }
  }

  /**
    * Index the given spectrum
    *
    * @param spectrum
    * @return the key at which the spectrum was indexed at
    */
  override def doIndex(spectrum: SimpleSpectrum): Index = {
    logger.trace(s"indexing: ${spectrum.splash}")
    indexMap.put(spectrum.splash, spectrum)
    this
  }

  /**
    * Returns the spectra matching the given key
    *
    * @param key
    * @return
    */
  override def get(key: Any): Iterable[SimpleSpectrum] = indexMap.values()

  /**
    * Size of this index
    *
    * @return
    */
  override def size: Int = indexMap.size()

  override def toString: String = this.getClass.getSimpleName + "(linear)" + "(" + binningMethod + ")"
}
