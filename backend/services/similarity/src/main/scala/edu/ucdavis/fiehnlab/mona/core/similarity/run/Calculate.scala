package edu.ucdavis.fiehnlab.mona.core.similarity.run

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.core.similarity.index.Index
import edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity.Similarity
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{ComputationalResult, SimpleSpectrum}

/**
  * provides us with simple standardized ways to calculated spectra against each other and ensure that
  * everything is up to code
  */
trait Calculate extends LazyLogging {

  /**
    *
    * @param unknown
    * @param library
    * @return
    */
  protected def calculate(unknown: SimpleSpectrum, library: Iterable[SimpleSpectrum], threshold: Double, algorithm: Similarity): Iterable[ComputationalResult]

  /**
    *
    * @param unknown
    * @param index
    * @param threshold
    * @return
    */
  final def calculate(unknown: SimpleSpectrum, index: Index, threshold: Double, algorithm: Similarity): Iterable[ComputationalResult] = {

    // Utilize the internal index
    calculate(index.binningMethod.binSpectrum(unknown), index.get(unknown), threshold, algorithm)
  }
}

/**
  * Easy access to a default implementation of our thread runner
  */
object Calculate {

  def create: Calculate = new MultiThreadRunner
}





