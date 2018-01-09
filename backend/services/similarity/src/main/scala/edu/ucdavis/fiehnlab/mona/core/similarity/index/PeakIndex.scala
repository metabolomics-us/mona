package edu.ucdavis.fiehnlab.mona.core.similarity.index

import java.util

import edu.ucdavis.fiehnlab.mona.core.similarity.index.cache.SpectrumCache
import edu.ucdavis.fiehnlab.mona.core.similarity.math.binning.{BinByRoundingMethod, BinningMethod}
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{BinnedSimpleSpectrum, SimpleSpectrum}
import edu.ucdavis.fiehnlab.mona.core.similarity.util.SpectrumUtils

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 2/7/17.
  */
class PeakIndex(override val binningMethod: BinningMethod, override val cache: SpectrumCache) extends Index(binningMethod, cache) {

  val internalIndex: Index = new LinearIndex(binningMethod, cache)

  def this() = this(new BinByRoundingMethod, SpectrumCache.create())

  /**
    * Internal index structure
    */
  val indexMap: java.util.Map[Int, java.util.Set[SimpleSpectrum]] = createBacking()

  /**
    * Defines the collection to use for the backing
    */
  def createBacking(): java.util.Map[Int, java.util.Set[SimpleSpectrum]] =
    new java.util.concurrent.ConcurrentHashMap[Int, util.Set[SimpleSpectrum]]()

  /**
    * Defined the backing for the internal association with keys
    *
    * @return
    */
  def createAssociationBacking(): util.Set[SimpleSpectrum] = new util.HashSet[SimpleSpectrum]


  /**
    * Searches in the index for the given spectrum, which should be done by equality and
    * might have linear performance depending of implementation
    *
    * @param spectrum
    * @return
    */
  override def lookup(spectrum: SimpleSpectrum): Option[SimpleSpectrum] = {
    logger.trace(s"looking up: ${spectrum.splash}")

    get(spectrum) match {
      case null => None
      case x: Iterable[SimpleSpectrum] => Some(x.head)
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

    spectrum.ions
      //.filter(_.intensity >= 0.05 * spectrum.maximumIon.intensity) // Optional filter, excluding for indexing
      .foreach { x =>
      val key: Int = SpectrumUtils.roundMZ(x.mz)

      if (!indexMap.containsKey(key)) {
        indexMap.put(key, createAssociationBacking())
      }

      indexMap.get(key).add(spectrum)

      // Track it in the internal index
      internalIndex.index(spectrum)
    }

    this
  }

  /**
    * Returns the spectra matching the given key
    *
    * @param key
    * @return
    */
  override def get(key: Any): Set[SimpleSpectrum] = key match {

    case mz: Int =>
      logger.trace(s"Looking up integer m/z value $mz")

      if (indexMap.containsKey(mz)) {
        indexMap.get(mz).asScala.toSet[SimpleSpectrum]
      } else {
        Set.empty[SimpleSpectrum]
      }

    case mz: Double =>
      logger.trace(s"Looking up floating-point m/z value $mz")
      get(SpectrumUtils.roundMZ(mz))

    // Search for all spectra that match all ions over 5% of the base peak intensity
    case spectrum: BinnedSimpleSpectrum =>
      logger.trace("Looking up binned simple spectrum")

      spectrum.ions
        .filter(_.intensity >= 0.05 * spectrum.maximumIon.intensity) // Optional filter
        .map(x => get(x.mz.toInt))
        .reduceLeft((x, y) => x.intersect(y))

    // Bin the spectrum and then search
    case spectrum: SimpleSpectrum =>
      logger.trace("Looking up simple spectrum")
      get(binningMethod.binSpectrum(spectrum))

    // Create a spectrum object from a list of peaks and assign the same intensity to every ion
    case ions: Array[_] => get(ions.toSeq)

    case Seq(mz, tail@_*) =>
      logger.trace("Looking up Seq of m/z values")

      if (tail.isEmpty) {
        get(mz)
      } else {
        get(mz).intersect(get(tail))
      }

    case x =>
      logger.warn(s"Object type ${x.getClass} not recognized")
      Set.empty[SimpleSpectrum]
  }

  /**
    * Size of this index
    *
    * @return
    */
  override def size: Int = indexMap.size()

  override def toString: String = this.getClass.getSimpleName + "(linear)" + "(" + binningMethod + ")"
}
