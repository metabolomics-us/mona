package edu.ucdavis.fiehnlab.mona.core.similarity.index

import java.util

import edu.ucdavis.fiehnlab.mona.core.similarity.index.cache.SpectrumCache
import edu.ucdavis.fiehnlab.mona.core.similarity.math.binning.{BinningMethod, NoBinningMethod}
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

import scala.collection.JavaConversions._

/**
  * Created by wohlgemuth on 1/28/16.
  */
abstract class StringBasedMapIndex(binningMethod: BinningMethod, cache: SpectrumCache) extends Index(binningMethod, cache) {

  val internalIndex: Index = new LinearIndex(binningMethod, cache)

  def this() = this(new NoBinningMethod, SpectrumCache.create())


  /**
    * Internal index structure
    */
  val indexMap: java.util.Map[String, java.util.Set[SimpleSpectrum]] = createBacking()

  /**
    * Defines the collection to use for the backing
    */
  def createBacking(): java.util.Map[String, java.util.Set[SimpleSpectrum]] =
    new java.util.concurrent.ConcurrentHashMap[String, util.Set[SimpleSpectrum]]()

  /**
    * Defined the backing for the internal association with keys
    *
    * @return
    */
  def createAssociationBacking(): util.Set[SimpleSpectrum] = new util.HashSet[SimpleSpectrum]

  /**
    * Index the given spectrum
    *
    * @param spectrum
    * @return the key where the spectra was indexed add
    */
  override def doIndex(spectrum: SimpleSpectrum): Index = {
    val key: String = buildKey(spectrum)

    if (!indexMap.contains(key)) {
      indexMap.put(key, createAssociationBacking())
    }

    indexMap.get(key).add(spectrum)

    // Track it in the internal index
    internalIndex.index(spectrum)

    this
  }

  /**
    * Searches in the index for the given spectrum
    *
    * @param spectrum
    * @return
    */
  override def lookup(spectrum: SimpleSpectrum): Option[SimpleSpectrum] = {
    val filteredData: Iterable[SimpleSpectrum] = get(spectrum)

    logger.debug(s"filtered data size: ${filteredData.size} of ${internalIndex.size} spectra")

    // Find identical spectra in the associated collection
    val result = filteredData.par.find(s => s.splash == spectrum.splash)

    if (result.isEmpty) {
      internalIndex.lookup(spectrum)
    } else {
      result
    }
  }

  /**
    * Builds the internal key to utilize
    *
    * @param key
    * @return
    */
  def buildKey(key: Any): String

  /**
    * Returns the spectra matching the given key
    *
    * @param key
    * @return
    */
  override def get(key: Any): Iterable[SimpleSpectrum] = key match {
    case a: SimpleSpectrum =>
      val internalKey: String = buildKey(key)
      searchIndex(a, internalKey)

    case _ =>
      List.empty
  }

  /**
    *
    * @param a
    * @param internalKey
    * @return
    */
  def searchIndex(a: SimpleSpectrum, internalKey: String): Iterable[SimpleSpectrum] = {

    if (indexMap.containsKey(internalKey)) {
      logger.debug(s"utilized key: $internalKey")

      indexMap.get(internalKey).toSet[SimpleSpectrum]
    } else {
      List.empty
    }
  }

  /**
    * Size of this index
    *
    * @return
    */
  override def size: Int = internalIndex.size

  /**
    * Returns the size of the internal map of this index
    *
    * @return
    */
  def countOfBuckets: Int = indexMap.size()
}
