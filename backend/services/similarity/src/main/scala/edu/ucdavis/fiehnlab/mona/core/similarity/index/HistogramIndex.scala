package edu.ucdavis.fiehnlab.mona.core.similarity.index

import edu.ucdavis.fiehnlab.mona.core.similarity.index.cache.SpectrumCache
import edu.ucdavis.fiehnlab.mona.core.similarity.math.binning.{BinningMethod, NoBinningMethod}
import edu.ucdavis.fiehnlab.mona.core.similarity.math.histogram.Histogram
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

/**
  * Created by wohlgemuth on 1/26/16.
  */
class HistogramIndex(binningMethod: BinningMethod, cache: SpectrumCache, val histogram: Histogram) extends StringBasedMapIndex(binningMethod, cache) {

  def this() = this(new NoBinningMethod, SpectrumCache.create(), Histogram.create())

  /**
    * Builds the internal key to utilize
    * @param key
    * @return
    */
  override def buildKey(key: Any): String = key match {
    case key: SimpleSpectrum => histogram.generate(key)
    case _ => key.toString
  }

  override def toString: String = s"${getClass.getSimpleName}($histogram)($binningMethod)"
}
