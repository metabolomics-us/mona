package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.types

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.groups._

case class MassBankRecord(
                           recordSpecificGroup: RecordSpecificGroup,
                           chemicalGroup: ChemicalGroup,
                           sampleGroup: SampleGroup,
                           analyticalChemistryGroup: AnalyticalChemistryGroup,
                           massSpectralDataGroup: MassSpectralDataGroup,
                           massSpectraPeakDataGroup: MassSpectralPeakDataGroup
                         )

case class PeakTriple(mz: Double, absInt: Double, relInt: Double)

case class PeakData(preprocessedPeaks: List[PeakTriple]) {
  private def computeRelativeIntensities(ps: List[PeakTriple]): List[PeakTriple] =
    if (ps.nonEmpty) {
      val x = ps.map(_.absInt).max
      ps.map(p => p.copy(relInt = math.round(p.absInt / x * 999f)))
    }
    else ps

  lazy val peaks: List[PeakTriple] = computeRelativeIntensities(preprocessedPeaks)
}

object PeakData {
  val empty = PeakData(List.empty)
}
