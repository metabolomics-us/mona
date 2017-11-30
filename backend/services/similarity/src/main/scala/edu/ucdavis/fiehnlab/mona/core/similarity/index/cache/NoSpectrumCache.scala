package edu.ucdavis.fiehnlab.mona.core.similarity.index.cache

import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

/**
  * A cache which is not a cache at all
  */
class NoSpectrumCache extends SpectrumCache {

  /**
    * adds a spectrum to the cache
    *
    * @param spectrum
    * @return
    */
  override def add(spectrum: SimpleSpectrum): SpectrumCache = this

  /**
    * fetches a spectrum from the cache
    *
    * @param spectrum
    * @return
    */
  override def get(spectrum: SimpleSpectrum): SimpleSpectrum = spectrum

  /**
    * does the cache already contains the spectrum
    *
    * @param spectrum
    * @return
    */
  override def contains(spectrum: SimpleSpectrum): Boolean = false
}
