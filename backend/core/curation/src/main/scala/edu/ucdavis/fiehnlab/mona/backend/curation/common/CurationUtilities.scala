package edu.ucdavis.fiehnlab.mona.backend.curation.common

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Spectrum}

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
}
