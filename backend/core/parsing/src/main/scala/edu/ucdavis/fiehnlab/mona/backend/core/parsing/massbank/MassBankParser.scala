package edu.ucdavis.fiehnlab.mona.backend.core.parsing.massbank

import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import jp.riken.massbank.reader.scala.MassBankRecordReader
import jp.riken.massbank.reader.scala.groups.{MassBankGroup, RecordSpecificGroup}
import jp.riken.massbank.reader.scala.types.{MassBankRecord, PeakData}

import scala.io.Source

trait MassBankParser {
  def parse(src: Source) = MassBankRecordReader.read(src).map(record => recordToSpectrum(record))

  def parse(str: String) = MassBankRecordReader.read(str).map(record => recordToSpectrum(record))

  def recordToSpectrum(rec: MassBankRecord): Spectrum = {
    Spectrum(
      id = null,
      lastUpdated = null,
      score = null,

      metaData = extractMetadata(rec),
      biologicalCompound = extractBiologicalCompound(rec),
      chemicalCompound = extractChemicalCompound(rec),

      spectrum = formatPeaks(rec.massSpectraPeakDataGroup.peak),
      splash = null,

      submitter = Submitter("", "", "", "", ""),
      authors = Array.empty,
      tags = Array.empty,
      library = Library("", "", "")
    )
  }

  /** Helper function to extract optional metadata fields from MassBankRecord parse tree */
  private def ifExists(field: Option[String])
    (name: String,
      category: String = "none",
      computed: Boolean = false,
      hidden: Boolean = false,
      score: Score = null,
      unit: String = null,
      url: String = null
    ): Option[MetaData] =
    field.map { value =>
      MetaData(category, computed, hidden, name, score, unit, url, value)
    }

  def extractMetadata(r: MassBankRecord): Array[MetaData] = {
    val dataProcessing = r.massSpectralDataGroup.dataProcessing.map({
      case (key, value) =>
        ifExists(Some(key))(name = "data processing action", category = "data transformation") // MS:1000543
    }).flatten

    Array(
      ifExists(r.recordSpecificGroup.accession)(name = "external reference identifier"), // MS:1000878

      ifExists(r.recordSpecificGroup.recordTitle)(name = "spectrum title", category = "spectrum attribute"), // MS:1000796
      ifExists(r.recordSpecificGroup.date)(name = "date"),
      ifExists(r.recordSpecificGroup.authors)(name = "co-author", category = "contact role"), // MS:1002036
      ifExists(r.recordSpecificGroup.license)(name = "license"),
      ifExists(r.recordSpecificGroup.copyright)(name = "copyright"),
      ifExists(r.recordSpecificGroup.publication)(name = "publication"),
      ifExists({
        if (r.recordSpecificGroup.comment.isEmpty) None
        else Some(r.recordSpecificGroup.comment.mkString("\n"))
      })(name = "comment"),


      // Mass spectra group
      ifExists(r.massSpectralDataGroup.focusedIon.get("PRECURSOR_M/Z"))
        (name = "base peak m/z", category = "spectrum attribute"), // MS:1000504
      ifExists(r.massSpectralDataGroup.focusedIon.get("PRECURSOR_TYPE"))
        (name = "isolation window attribute", category = "object attribute"), // MS:1000792

    ).flatten ++ dataProcessing
  }

  private def emptyCompound = Compound(
    inchi = "",
    inchiKey = "",
    metaData = Array.empty,
    molFile = "",
    names = Array.empty,
    tags = Array.empty)

  def extractBiologicalCompound(r: MassBankRecord): Compound = {
    emptyCompound
  }

  // TODO CH group, double-check AC group
  def extractChemicalCompound(r: MassBankRecord): Compound = {
    val base = List(
      ifExists(r.analyticalChemistryGroup.instrument)(name = "instrument model", category = "instrument"), // MS:1000031
      ifExists(r.analyticalChemistryGroup.instrumentType)(name = "ionization type", category = "source"), // MS:1000008
      ifExists(r.analyticalChemistryGroup.ionMode)(name = "scan polarity", category = "scan"), // MS:1000465
      ifExists(r.analyticalChemistryGroup.msType)(name = "", category = "scan"), // MS:1000465
    ).flatten

    val massSpec = List(
      // Additional ontology terms from MassBank record specification
      ifExists(r.analyticalChemistryGroup.massSpectrometry.get("COLLISION_GAS"))
        (name = "collision gas", category = "precursor activation attribute"),  // MS:1000419
      ifExists(r.analyticalChemistryGroup.massSpectrometry.get("RETENTION_TIME"))
        (name = "scan start time", category = "scan attribute") // MS:1000016
      ) ++
        (r.analyticalChemistryGroup.massSpectrometry -- List("COLLISION_GAS", "RETENTION_TIME"))
          .map({ case (key, value) => ifExists(Some(key))(name = key) })

    // For each key -> val1 :: val2 :: ..., produce metadata for (key -> val1) :: (key -> val2) :: ...
    val chromatography = r.analyticalChemistryGroup.chromatography.flatMap({
      case (key, value) =>
        value.map(v => MetaData("none", false, false, null, null, null, key, v))
    })

    val others = r.analyticalChemistryGroup.other.map({ case (key, value) => ifExists(Some(key))(name = key) })

    emptyCompound.copy(
      metaData = (base ++ massSpec.flatten ++ chromatography ++ others.flatten).toArray
    )
  }

  def formatPeaks(ps: PeakData): String = {
    if (ps.peaks.isEmpty) null
    else ps.peaks.map(triple => s"${triple.mz}:${triple.absInt}").mkString(" ")
  }
}
object MassBankParser extends MassBankParser
