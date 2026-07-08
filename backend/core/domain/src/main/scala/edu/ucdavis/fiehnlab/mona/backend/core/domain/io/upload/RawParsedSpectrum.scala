package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.upload

/**
  * One metadata row as produced by a raw format parser, before origin tagging and the
  * name/category normalization UploadSpectrumBuilder applies
  */
case class RawMetaEntry(name: String, value: String, category: Option[String] = None)

/**
  * Intermediate shape emitted by MSPReader/MGFReader/MassBankReader, mirroring the raw object
  * each of the three client side parsers builds before upload-library.service.ts's enrichment
  * step (processData/submitSpectrum) turns it into a persistable Spectrum. Kept separate from
  * the Spectrum domain entity so the readers stay format parsing only
  */
case class RawParsedSpectrum(
  names: List[String] = Nil,
  name: Option[String] = None,
  meta: List[RawMetaEntry] = Nil,
  spectrum: String = "",
  accurate: Boolean = false,
  inchiKey: Option[String] = None,
  inchi: Option[String] = None,
  smiles: Option[String] = None
)
