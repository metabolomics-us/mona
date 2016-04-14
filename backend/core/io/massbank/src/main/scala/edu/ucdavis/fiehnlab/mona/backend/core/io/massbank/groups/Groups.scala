package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.groups

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.types._

sealed trait MassBankGroup

case class RecordSpecificGroup(
  accession: Option[String],
  recordTitle: Option[String],
  date: Option[String],
  authors: Option[String],
  license: Option[String],
  copyright: Option[String],
  publication: Option[String],
  comment: List[String],
  other: Map[String, List[String]] = Map.empty
) extends MassBankGroup

case class ChemicalGroup(
  name: List[String],
  compoundClass: Option[String],
  formula: Option[String],
  exactMass: Option[String],
  smiles: Option[String],
  iupac: Option[String],
  link: Map[String, String],
  other: Map[String, List[String]] = Map.empty
) extends MassBankGroup

case class SampleGroup(
  scientificName: Option[String],
  lineage: Option[String],
  link: Map[String, String],
  sample: Option[String],
  other: Map[String, List[String]] = Map.empty
) extends MassBankGroup

case class AnalyticalChemistryGroup(
    instrument: Option[String],
    instrumentType: Option[String],
    massSpectrometry: Map[String, String],
    chromatography: Map[String, List[String]],
    other: Map[String, List[String]] = Map.empty
) extends MassBankGroup {
  lazy val ionMode: Option[String] = massSpectrometry.get("ION_MODE")
  lazy val msType: Option[String] = massSpectrometry.get("MS_TYPE")
}

case class MassSpectralDataGroup(
  focusedIon: Map[String, String],
  dataProcessing: Map[String, String],
  other: Map[String, List[String]] = Map.empty
) extends MassBankGroup

case class MassSpectralPeakDataGroup(
  splash: Option[String],
  annotation: List[String],
  numPeak: Int,
  peak: PeakData,
  other: Map[String, List[String]] = Map.empty
) extends MassBankGroup

