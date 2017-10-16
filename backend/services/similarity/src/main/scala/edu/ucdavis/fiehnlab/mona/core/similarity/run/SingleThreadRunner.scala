package edu.ucdavis.fiehnlab.mona.core.similarity.run

import edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity.Similarity
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{ComputationalResult, SimpleSpectrum}

/**
  * Created by sajjan on 12/13/16.
  */
class SingleThreadRunner extends Calculate {
  /**
    * calculates one spectrum against a list of unknown spectra
    *
    * @param unknown
    * @param library
    * @return
    */
  override def calculate(unknown: SimpleSpectrum, library: Iterable[SimpleSpectrum], threshold: Double, algorithm: Similarity): Iterable[ComputationalResult] = {
    library
      .map(spectrum => ComputationalResult(unknown, spectrum, algorithm.compute(unknown, spectrum)))
      .filter(_.score >= threshold)
  }
}
