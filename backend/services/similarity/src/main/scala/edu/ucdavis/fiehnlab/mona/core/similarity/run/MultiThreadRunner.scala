package edu.ucdavis.fiehnlab.mona.core.similarity.run

import edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity.Similarity
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{ComputationalResult, SimpleSpectrum}
import scala.collection.parallel.CollectionConverters._

/**
  * Created by sajjan on 12/13/16.
  */
class MultiThreadRunner extends Calculate {
  /**
    * calculates one spectrum against a list of unknown spectra
    *
    * @param unknown
    * @param library
    * @return
    */
  override def calculate(unknown: SimpleSpectrum, library: Iterable[SimpleSpectrum], threshold: Double, algorithm: Similarity, precursorToleranceDa: Double = .01, removePrecursorIon: Boolean): Iterable[ComputationalResult] = {
      library.par
        .filter(!_.precursorMZ.isNaN)
        .filter(x => {calculateAbsolute(unknown.precursorMZ, x.precursorMZ) <= precursorToleranceDa})
        .map(spectrum => ComputationalResult(unknown, spectrum, algorithm.compute(unknown, spectrum, removePrecursorIon)))
        .filter(_.score >= threshold)
        .seq
  }
}
