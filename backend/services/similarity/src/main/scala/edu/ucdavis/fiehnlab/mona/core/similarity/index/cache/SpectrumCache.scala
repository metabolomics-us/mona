package edu.ucdavis.fiehnlab.mona.core.similarity.index.cache

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

/**
  * Created by wohlgemuth on 2/4/16.
  */
trait SpectrumCache {

  /**
    * adds a spectrum to the cache
    *
    * @param spectrum
    * @return
    */
  def add(spectrum: SimpleSpectrum): SpectrumCache

  /**
    * does the cache already contains the spectrum
    *
    * @param spectrum
    * @return
    */
  def contains(spectrum: SimpleSpectrum): Boolean

  /**
    * fetches a spectrum from the cache
    *
    * @param spectrum
    * @return
    */
  def get(spectrum: SimpleSpectrum): SimpleSpectrum
}


/**
  * single ton to give us access to one and only 1 cache
  */
object SpectrumCache extends LazyLogging {

  logger.info("Created SpectrumCache instance")

  private val impl: SpectrumCache = new MapBasedSpectrumCache(new util.concurrent.ConcurrentHashMap[String, SimpleSpectrum](1000000, 0.7f))

  def create(): SpectrumCache = impl
}



