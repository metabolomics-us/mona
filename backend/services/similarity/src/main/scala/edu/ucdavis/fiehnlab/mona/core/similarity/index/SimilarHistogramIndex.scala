package edu.ucdavis.fiehnlab.mona.core.similarity.index

import edu.ucdavis.fiehnlab.mona.core.similarity.index.cache.SpectrumCache
import edu.ucdavis.fiehnlab.mona.core.similarity.math.binning.{BinningMethod, NoBinningMethod}
import edu.ucdavis.fiehnlab.mona.core.similarity.math.histogram.Histogram
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum
import edu.ucdavis.fiehnlab.mona.core.similarity.util.SpectrumUtils

import scala.collection.JavaConversions._


/**
  * Created by wohlgemuth on 2/4/16.
  */
class SimilarHistogramIndex(binningMethod: BinningMethod, cache: SpectrumCache, histogram: Histogram, val minSimiarity: Double) extends HistogramIndex(binningMethod, cache, histogram) {

  def this() = this(new NoBinningMethod, SpectrumCache.create(), Histogram.create(), 0.9)

  /**
    * Searches against this index, using the provided similarity measure and tolerance setting
    *
    * @param spectrum
    * @param internalKey
    * @return
    */
  override def searchIndex(spectrum: SimpleSpectrum, internalKey: String): Iterable[SimpleSpectrum] = {
    val keys = indexMap.keySet.filter(s => isSimilar(spectrum.histogram, s))

    logger.info(s"Keys were reduced by ${100 - keys.size / indexMap.size() * 100}% from ${indexMap.size()} to ${keys.size}")

    keys.flatMap(indexMap.get(_))
  }

  /**
    * are these 2 histograms similar to each other
    *
    * @param hist1
    * @param hist2
    * @return
    */
  def isSimilar(hist1: String, hist2: String): Boolean = {
    SpectrumUtils.calculateHistogramSimilarity(hist1, hist2, 10) >= minSimiarity
  }
}
