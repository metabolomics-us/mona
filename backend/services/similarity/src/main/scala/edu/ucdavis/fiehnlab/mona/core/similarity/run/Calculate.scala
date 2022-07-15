package edu.ucdavis.fiehnlab.mona.core.similarity.run

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData}
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
  protected def calculate(unknown: SimpleSpectrum, library: Iterable[SimpleSpectrum], threshold: Double, algorithm: Similarity, precursorToleranceDa: Double, removePrecursorIon: Boolean, calculateAllAdducts: Boolean): Iterable[ComputationalResult]

  /**
    *
    * @param unknown
    * @param index
    * @param threshold
    * @return
    */
  final def calculate(unknown: SimpleSpectrum, index: Index, threshold: Double, algorithm: Similarity, precursorToleranceDa: Double, removePrecursorIon: Boolean, calculateAllAdducts: Boolean): Iterable[ComputationalResult] = {

    // Utilize the internal index
    calculate(index.binningMethod.binSpectrum(unknown), index.get(unknown), threshold, algorithm, precursorToleranceDa, removePrecursorIon, calculateAllAdducts)
  }

  def calculateAbsolute(unknownPrecursorMZ: Double, referencePrecursorMZ: Double): Double = {
    Math.abs(unknownPrecursorMZ - referencePrecursorMZ)
  }

  def findAdductMatch(unknownPrecursorMz: Double, precursorToleranceDa: Double, knownCompounds: Array[Compound]): Boolean = {
    val biologicalCompound: Compound =
      if (knownCompounds.exists(_.kind == "biological")) {
        knownCompounds.find(_.kind == "biological").head
      } else if (knownCompounds.nonEmpty) {
        knownCompounds.head
      } else {
        null
      }

    if (biologicalCompound == null) {
      logger.debug(s"Compound not on spectra")
      false
    } else {
      val theoreticalAdducts: Array[MetaData] = biologicalCompound.metaData.filter(x => x.category == "theoretical adduct")
      if(theoreticalAdducts.length == 0) {
        false
      } else {
        theoreticalAdducts.exists(x => calculateAbsolute(unknownPrecursorMz, x.value.asInstanceOf[Double]) <= precursorToleranceDa)
      }

    }
  }
}

/**
  * Easy access to a default implementation of our thread runner
  */
object Calculate {

  def create: Calculate = new MultiThreadRunner
}





