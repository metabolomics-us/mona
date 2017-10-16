package edu.ucdavis.fiehnlab.mona.backend.curation.util

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Impact, Score, Spectrum}

/**
  * Created by sajjan on 4/20/16.
  */
object CurationUtilities {
  final def getCompounds(s: Spectrum, kind: String): Array[Compound] = s.compound.filter(_.kind == kind)

  final def getBiologicalCompounds(s: Spectrum): Array[Compound] = getCompounds(s, "biological")
  final def getChemicalCompounds(s: Spectrum): Array[Compound] = getCompounds(s, "chemical")
  final def getPredictedCompounds(s: Spectrum): Array[Compound] = getCompounds(s, "predicted")

  final def getFirstBiologicalCompound(s: Spectrum): Compound = getBiologicalCompounds(s).headOption.orNull
  final def getFirstChemicalCompound(s: Spectrum): Compound = getChemicalCompounds(s).headOption.orNull
  final def getFirstPredictedCompound(s: Spectrum): Compound = getPredictedCompounds(s).headOption.orNull

  /**
    *
    * @param score
    * @param impactValue
    * @param impactReason
    * @return
    */
  final def addImpact(score: Score, impactValue: Double, impactReason: String): Score = {
    if (score == null || score.impacts == null) {
      Score(Array(Impact(impactValue, impactReason)), 0.0)
    } else {
      score.copy(impacts = score.impacts :+ Impact(impactValue, impactReason))
    }
  }
}
