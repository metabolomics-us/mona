package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank

/** Defines constant tags used in MassBank records. Use to reduce misspelled tag names in code. */
trait TagNames {
  val `ACCESSION` = "ACCESSION"
  val `RECORD_TITLE` = "RECORD_TITLE"
  val `DATE` = "DATE"
  val `AUTHORS` = "AUTHORS"
  val `LICENSE` = "LICENSE"
  val `COPYRIGHT` = "COPYRIGHT"
  val `PUBLICATION` = "PUBLICATION"
  val `COMMENT` = "COMMENT"

  val `CH:NAME` = "CH$NAME"
  val `CH:COMPOUND_CLASS` = "CH$COMPOUND_CLASS"
  val `CH:FORMULA` = "CH$FORMULA"
  val `CH:EXACT_MASS` = "CH$EXACT_MASS"
  val `CH:SMILES` = "CH$SMILES"
  val `CH:IUPAC` = "CH$IUPAC"
  val `CH:LINK` = "CH$LINK"

  val `SP:SCIENTIFIC_NAME` = "SP$SCIENTIFIC_NAME"
  val `SP:LINEAGE` = "SP$LINEAGE"
  val `SP:LINK` = "SP$LINK"
  val `SP:SAMPLE` = "SP$SAMPLE"

  val `AC:INSTRUMENT` = "AC$INSTRUMENT"
  val `AC:INSTRUMENT_TYPE` = "AC$INSTRUMENT_TYPE"
  val `AC:MASS_SPECTROMETRY` = "AC$MASS_SPECTROMETRY"
  val `AC:MASS_SPECTROMETRY: MS_TYPE` = "AC$MASS_SPECTROMETRY: MS_TYPE"
  val `AC:MASS_SPECTROMETRY: ION_MODE` = "AC$MASS_SPECTROMETRY: ION_MODE"
  val `AC:CHROMATOGRAPHY` = "AC$CHROMATOGRAPHY"

  val `MS:FOCUSED_ION` = "MS$FOCUSED_ION"
  val `MS:DATA_PROCESSING` = "MS$DATA_PROCESSING"

  val `PK:SPLASH` = "PK$SPLASH"
  val `PK:ANNOTATION` = "PK$ANNOTATION"
  val `PK:NUM_PEAK` = "PK$NUM_PEAK"
  val `PK:PEAK` = "PK$PEAK"
}
object TagNames extends TagNames
