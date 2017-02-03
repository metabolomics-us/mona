package edu.ucdavis.fiehnlab.mona.core.similarity.index

import edu.ucdavis.fiehnlab.mona.core.similarity.index.cache.SpectrumCache
import edu.ucdavis.fiehnlab.mona.core.similarity.math.binning.BinningMethod
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum

/**
  * A sequential index which can be constructed from several indexes instead of just one.
  * The priority of the indexes is defined by there position in the given sequence
  *
  * @param binningMethod
  * @param cache
  * @param indexes
  */
class SequentialIndex(override val binningMethod: BinningMethod, override val cache: SpectrumCache, val indexes: Seq[Index])extends Index(binningMethod, cache) {

  val internalIndex: Index = new LinearIndex(binningMethod, cache)

  /**
    * Index the given spectrum
    *
    * @param spectrum
    * @return
    */
  override protected def doIndex(spectrum: SimpleSpectrum): Index = {

    indexes.foreach({
      _.index(spectrum)
    })

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

    indexes.foreach({ idx =>
      idx.lookup(spectrum) match {
        case Some(i) => Some(i)
        case None =>
      }
    })

    None
  }

  /**
    * Returns the spectra matching the given key
    *
    * @param key
    * @return
    */
  override def get(key: Any): Iterable[SimpleSpectrum] = {
    indexes.foreach({ idx =>
      val result = idx.get(key)

      if(result.nonEmpty) {
        result
      }
    })

    List.empty[SimpleSpectrum]
  }

  /**
    * an index has a certain size
    *
    * @return
    */
  override def size: Int = internalIndex.size
}