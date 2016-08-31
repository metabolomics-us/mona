package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank

import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.TagNames._
import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.types.{MassBankRecord, PeakData}
import scala.io.Source

trait MassBankToSpectrumMapper {
  def parse(src: Source) = MassBankRecordReader.read(src).map(record => recordToSpectrum(record))

  def parse(str: String) = MassBankRecordReader.read(str).map(record => recordToSpectrum(record))

  /** One-to-one mapping of MassBank record fields to base metadata fields */
  def recordToSpectrum(record: MassBankRecord): Spectrum = {
    Spectrum(
      id = record.recordSpecificGroup.accession orNull,
      lastUpdated = null,
      score = null,

      metaData = extractMetadata(record),
      annotations = Array(),
      compound = Array(extractBiologicalCompound(record)),

      spectrum = formatPeaks(record.massSpectraPeakDataGroup.peak),
      splash = null,

      submitter = null,
      authors = null,
      tags = null,
      library = null
    )
  }

  /** Remove the tag name's parent (e.g., AC$TAG -> TAG) */
  private def simplifyTag(tag: String): String = tag.drop(tag.indexOf('$') + 1)

  /** Helper function to decrease verbosity of creating metadata */
  private def meta[T](
    name: String,
    value: T,
    category: Option[String] = None,
    computed: Boolean = false,
    hidden: Boolean = false,
    score: Score = null,
    unit: String = null,
    url: String = null
  ): MetaData =
    MetaData(category.map(simplifyTag) getOrElse "none", computed, hidden, simplifyTag(name), score, unit, url, value)

  /** Helper function to extract optional metadata fields from MassBankRecord parse tree */
  private def ifExists(name: String, field: Option[String]): Option[MetaData] = field.map { value => meta(name, value) }

  /** Helper function to convert parse tree `Map[String, String]` to metadata */
  private def mapToMetaList(map: Map[String, String], category: Option[String] = None): List[MetaData] =
    map.map({ case (key, value) => meta(key, value , category) }).toList

  /** Helper function to convert parse tree `List[String]` to metadata */
  private def listToMetaList(name: String, list: List[String], category: Option[String] = None): List[MetaData] =
    if (list.isEmpty) List.empty else list.map(value => meta(name, value, category))

  /** Helper function to convert parse tree `Map[String, List[String]]` to metadata */
  private def mapListToMetaList(mapList: Map[String, List[String]], category: Option[String] = None): List[MetaData] =
    mapList.flatMap({ case (key, list) => listToMetaList(key, list, category) }).toList

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
        List(
          ifExists(`CH:COMPOUND_CLASS`, r.chemicalGroup.compoundClass),
          ifExists(`CH:FORMULA`, r.chemicalGroup.formula),
          ifExists(`CH:EXACT_MASS`, r.chemicalGroup.exactMass),
          ifExists(`CH:SMILES`, r.chemicalGroup.smiles)
        ).flatten ++
        mapToMetaList(map = r.chemicalGroup.link, Some(`CH:LINK`)) ++
        mapListToMetaList(r.chemicalGroup.other)

    val SP: List[MetaData] = List(
      ifExists(`SP:SCIENTIFIC_NAME`, r.sampleGroup.scientificName),
      ifExists(`SP:LINEAGE`, r.sampleGroup.lineage),
      ifExists(`SP:SAMPLE`, r.sampleGroup.sample)
    ).flatten ++
      mapToMetaList(r.chemicalGroup.link, Some(`SP:LINK`)) ++
      mapListToMetaList(r.sampleGroup.other)

    val AC: List[MetaData] = List(
      ifExists(`AC:INSTRUMENT`, r.analyticalChemistryGroup.instrument),
      ifExists(`AC:INSTRUMENT_TYPE`, r.analyticalChemistryGroup.instrumentType)
    ).flatten ++
      mapToMetaList(r.analyticalChemistryGroup.massSpectrometry, Some(`AC:MASS_SPECTROMETRY`)) ++
      mapListToMetaList(r.analyticalChemistryGroup.chromatography, Some(`AC:CHROMATOGRAPHY`)) ++
      mapListToMetaList(r.analyticalChemistryGroup.other)

    val MS: List[MetaData] =
      mapToMetaList(r.massSpectralDataGroup.focusedIon, Some(`MS:FOCUSED_ION`)) ++
        mapToMetaList(r.massSpectralDataGroup.dataProcessing, Some(`MS:DATA_PROCESSING`)) ++
        mapListToMetaList(r.massSpectralDataGroup.other)

    (base ++ CH ++ SP ++ AC ++ MS).toArray
  }

  /** Convert peaks into space-delimited "m/z:absolute intensity" pairs */
  private def formatPeaks(ps: PeakData): String = {
    if (ps.peaks.isEmpty) null
    else ps.peaks.map(triple => s"${triple.mz}:${triple.absInt}").mkString(" ")
  }

  /** Generate biological compound information */
  private def extractBiologicalCompound(r: MassBankRecord): Compound = {
    def asName(n: String) = Names(false, n, 0.0, "user-provided")

    val names = r.chemicalGroup.name.map(asName).toArray
    val inchi = r.chemicalGroup.iupac orNull

    Compound(inchi, null, null, null, names, null, false, null, "biological")
  }
}

object MassBankToSpectrumMapper extends MassBankToSpectrumMapper
