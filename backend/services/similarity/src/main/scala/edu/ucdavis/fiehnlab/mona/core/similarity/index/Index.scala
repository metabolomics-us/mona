package edu.ucdavis.fiehnlab.mona.core.similarity.index

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.core.similarity.index.cache.SpectrumCache
import edu.ucdavis.fiehnlab.mona.core.similarity.math.binning.BinningMethod
import edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity.Similarity
import edu.ucdavis.fiehnlab.mona.core.similarity.run.Calculate
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{BinnedSimpleSpectrum, ComputationalResult, SimpleSpectrum}

/**
  * The definition of a spectral index
  *
  * It utilizes a cache to ensure that the system does not store duplicated spectra in different indexes
  */
abstract class Index(val binningMethod: BinningMethod, val cache: SpectrumCache) extends LazyLogging {

  /**
    * Index the given spectrum
    *
    * @param spectrum
    * @return
    */
  final def index(spectrum: SimpleSpectrum): Index = {

    spectrum match {
      case x: BinnedSimpleSpectrum =>
        if (!cache.contains(x))
          cache.add(x)

        doIndex(cache.get(x))
      case _ =>
        val binned = binningMethod.binSpectrum(spectrum)

        if (!cache.contains(binned))
          cache.add(binned)

        doIndex(cache.get(binned))
    }
  }

  /**
    * Index all spectra in the given collection
    *
    * @param spectra
    */
  final def index(spectra: Iterable[SimpleSpectrum]): Index = {

    spectra.par.foreach(index)
    this
  }

  /**
    * Index the given spectrum
    *
    * @param spectrum
    * @return the key at which the spectrum was indexed at
    */
  protected def doIndex(spectrum: SimpleSpectrum): Index

  /**
    * Returns the spectra matching the given key and should provide the fastest possible performance
    *
    * @param key
    * @return
    */
  def get(key: Any = None): Iterable[SimpleSpectrum]

  /**
    * Searches in the index for the given spectrum, which should be done by equality and
    * might have linear performance depending of implementation
    *
    * @param spectrum
    * @return
    */
  def lookup(spectrum: SimpleSpectrum): Option[SimpleSpectrum]

  /**
    * Searches against this index, using the provided similarity measure and tolerance setting. It is a
    * blocking search, which can generate rather large collections
    *
    * @param spectrum
    * @param similarity
    * @param threshold
    * @return
    */
  final def search(spectrum: SimpleSpectrum, similarity: Similarity, threshold: Double = 0.5): Iterable[ComputationalResult] = {

    Calculate.create.calculate(spectrum, this, threshold, similarity)
  }

  /**
    * Size of this index
    *
    * @return
    */
  def size: Int
}