package edu.ucdavis.fiehnlab.mona.core.similarity.run

import edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity.Similarity
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{ComputationalResult, ResultHandler, SimpleSpectrum}

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
  override def calculate(unknown: SimpleSpectrum, library: Iterable[SimpleSpectrum], tolerance: Double, algorithm: Similarity, resultHandler: ResultHandler) {
    library.foreach { spectrum =>
      val score = algorithm.compute(unknown, spectrum)

      if (score >= tolerance) {
        resultHandler.handleResult(ComputationalResult(unknown, spectrum, score))
      }
    }
  }
}
