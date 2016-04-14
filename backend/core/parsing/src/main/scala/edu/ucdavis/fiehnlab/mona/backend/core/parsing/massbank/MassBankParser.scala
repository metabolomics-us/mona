package edu.ucdavis.fiehnlab.mona.backend.core.parsing.massbank

import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import jp.riken.massbank.reader.scala.MassBankRecordReader
import jp.riken.massbank.reader.scala.TagNames._
import jp.riken.massbank.reader.scala.types.{MassBankRecord, PeakData}

import scala.io.Source

trait MassBankParser {
  def parse(src: Source) = MassBankRecordReader.read(src).map(record => recordToSpectrum(record))

  def parse(str: String) = MassBankRecordReader.read(str).map(record => recordToSpectrum(record))

  /** */
  def recordToSpectrum(record: MassBankRecord): Spectrum = {
    Spectrum(
      id = null,
      lastUpdated = null,
      score = null,

      metaData = extractMetadata(record),
      biologicalCompound = Compound(null, null, Array.empty, null, Array.empty, Array.empty),
      chemicalCompound = Compound(null, null, Array.empty, null, Array.empty, Array.empty),

      spectrum = formatPeaks(record.massSpectraPeakDataGroup.peak),
      splash = null,

      submitter = Submitter("", "", "", "", ""),
      authors = Array.empty,
      tags = Array.empty,
      library = Library("", "", "")
    )
  }

  /** Helper function to decrease verbosity of creating metadata */
  private def meta[T](
    name: String,
    value: T,
    category: String = "none",
    computed: Boolean = false,
    hidden: Boolean = false,
    score: Score = null,
    unit: String = null,
    url: String = null
  ): MetaData = MetaData(category, computed, hidden, name, score, unit, url, value)

  /** Helper function to extract optional metadata fields from MassBankRecord parse tree */
  private def ifExists(name: String, field: Option[String]): Option[MetaData] = field.map { value => meta(name, value) }

  /** Helper function to convert parse tree `Map[String, String]` to metadata */
  private def mapToMetaList(map: Map[String, String], prefix: Option[String] = None): List[MetaData] =
    map.map({ case (key, value) => meta(prefix.getOrElse("") + key, value) }).toList

  /** Helper function to convert parse tree `List[String]` to metadata */
  private def listToMetaList(name: String, list: List[String], prefix: Option[String] = None): List[MetaData] =
    if (list.isEmpty) List.empty else list.map(value => meta(prefix.getOrElse("") + name, value))

  /** Helper function to convert parse tree `Map[String, List[String]]` to metadata */
  private def mapListToMetaList(mapList: Map[String, List[String]], prefix: Option[String] = None): List[MetaData] =
    mapList.map({ case (key, list) => listToMetaList(key, list, prefix) }).flatten.toList

  /** Simple conversion of MassBank record fields into metadata */
  private def extractMetadata(r: MassBankRecord): Array[MetaData] = {
    val base: Iterable[MetaData] = List(
      ifExists(ACCESSION, r.recordSpecificGroup.accession),
      ifExists(RECORD_TITLE, r.recordSpecificGroup.recordTitle),
      ifExists(DATE, r.recordSpecificGroup.date),
      ifExists(AUTHORS, r.recordSpecificGroup.authors),
      ifExists(LICENSE, r.recordSpecificGroup.license),
      ifExists(COPYRIGHT, r.recordSpecificGroup.copyright),
      ifExists(PUBLICATION, r.recordSpecificGroup.publication)
    ).flatten ++
      listToMetaList(COMMENT, r.recordSpecificGroup.comment) ++
      mapListToMetaList(r.recordSpecificGroup.other)

    val CH: Iterable[MetaData] =
      listToMetaList(`CH:NAME`, r.chemicalGroup.name) ++
        List(
          ifExists(`CH:COMPOUND_CLASS`, r.chemicalGroup.compoundClass),
          ifExists(`CH:FORMULA`, r.chemicalGroup.formula),
          ifExists(`CH:EXACT_MASS`, r.chemicalGroup.exactMass),
          ifExists(`CH:SMILES`, r.chemicalGroup.smiles),
          ifExists(`CH:IUPAC`, r.chemicalGroup.iupac)
        ).flatten ++
        mapToMetaList(prefix = Some(`CH:LINK` + ": "), map = r.chemicalGroup.link) ++
        mapListToMetaList(r.chemicalGroup.other)

    val SP: List[MetaData] = List(
      ifExists(`SP:SCIENTIFIC_NAME`, r.sampleGroup.scientificName),
      ifExists(`SP:LINEAGE`, r.sampleGroup.lineage),
      ifExists(`SP:SAMPLE`, r.sampleGroup.sample)
    ).flatten ++
      mapToMetaList(prefix = Some(`SP:LINK` + ": "), map = r.chemicalGroup.link) ++
      mapListToMetaList(r.sampleGroup.other)

    val AC: List[MetaData] = List(
      ifExists(`AC:INSTRUMENT`, r.analyticalChemistryGroup.instrument),
      ifExists(`AC:INSTRUMENT_TYPE`, r.analyticalChemistryGroup.instrumentType)
    ).flatten ++
      mapToMetaList(prefix = Some(`AC:MASS_SPECTROMETRY` + ": "), map = r.analyticalChemistryGroup.massSpectrometry) ++
      mapListToMetaList(prefix = Some(`AC:CHROMATOGRAPHY` + ": "), mapList = r.analyticalChemistryGroup.chromatography) ++
      mapListToMetaList(r.analyticalChemistryGroup.other)

    val MS: List[MetaData] =
      mapToMetaList(prefix = Some(`MS:FOCUSED_ION` + ": "), map = r.massSpectralDataGroup.focusedIon) ++
        mapToMetaList(prefix = Some(`MS:DATA_PROCESSING` + ": "), map = r.massSpectralDataGroup.dataProcessing) ++
        mapListToMetaList(r.massSpectralDataGroup.other)

    (base ++ CH ++ SP ++ AC ++ MS).toArray
  }

  /** Convert peaks into space-delimited "m/z:absolute intensity" pairs */
  private def formatPeaks(ps: PeakData): String = {
    if (ps.peaks.isEmpty) null
    else ps.peaks.map(triple => s"${triple.mz}:${triple.absInt}").mkString(" ")
  }
}

object MassBankParser extends MassBankParser
