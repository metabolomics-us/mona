package edu.ucdavis.fiehnlab.mona.core.similarity.run

import edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity.Similarity
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{ComputationalResult, ResultHandler, SimpleSpectrum}

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
  override def calculate(unknown: SimpleSpectrum, library: Iterable[SimpleSpectrum], threshold: Double, algorithm: Similarity, resultHandler: ResultHandler) {
    library.par.foreach { spectrum =>
      val score = algorithm.compute(unknown, spectrum)

      if (score >= threshold) {
        resultHandler.handleResult(ComputationalResult(unknown, spectrum, score))
      }
    }
  }
}
