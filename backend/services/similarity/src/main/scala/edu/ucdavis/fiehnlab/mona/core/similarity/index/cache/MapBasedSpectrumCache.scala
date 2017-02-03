package edu.ucdavis.fiehnlab.mona.core.similarity.index.cache

import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

/**
  * simple map based cache, which utilizes splashes as keys. We might want to think about utilizing some other form of keys,
  * in case the same splash comes from the same source
  *
  * @param backing
  */
class MapBasedSpectrumCache(val backing: java.util.Map[String, SimpleSpectrum]) extends SpectrumCache {
  /**
    * adds a spectrum to the cache
    * @param spectrum
    * @return
    */
  override def add(spectrum: SimpleSpectrum): SpectrumCache = {
    backing.put(spectrum.splash, spectrum)
    this
  }

  /**
    * fetches a spectrum from the cache
    * @param spectrum
    * @return
    */
  override def get(spectrum: SimpleSpectrum): SimpleSpectrum = backing.get(spectrum.splash)

  /**
    * does the cache already contains the spectrum
    * @param spectrum
    * @return
    */
  override def contains(spectrum: SimpleSpectrum): Boolean = backing.containsKey(spectrum.splash)
}
