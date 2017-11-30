package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.TagNames._
import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.groups._
import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.types._

import scala.util.Try

trait RecordSpecificGroupParser extends FieldParsers {
  def recordSpecificGroup =
    fieldsStartingWith("", tag => !tag.contains("$")) ^^ {
      case fields =>
        RecordSpecificGroup(
          fields.getValue(`ACCESSION`),
          fields.getValue(`RECORD_TITLE`),
          fields.getValue(`DATE`),
          fields.getValue(`AUTHORS`),
          fields.getValue(`LICENSE`),
          fields.getValue(`COPYRIGHT`),
          fields.getValue(`PUBLICATION`),
          fields.getIterative(`COMMENT`),
          fields -- List(`ACCESSION`, `RECORD_TITLE`, `DATE`, `AUTHORS`, `LICENSE`, `COPYRIGHT`, `PUBLICATION`, `COMMENT`)
        )
    }
}

object RecordSpecificGroupParser extends RecordSpecificGroupParser

trait ChemicalGroupParser extends FieldParsers {
  def chemicalGroup =
    fieldsStartingWith("CH$") ^^ {
      case fields =>
        ChemicalGroup(
          fields.getIterative(`CH:NAME`),
          fields.getValue(`CH:COMPOUND_CLASS`),
          fields.getValue(`CH:FORMULA`),
          fields.getValue(`CH:EXACT_MASS`),
          fields.getValue(`CH:SMILES`),
          fields.getValue(`CH:IUPAC`),
          fields.getSubtags(`CH:LINK`),
          fields -- List(`CH:NAME`, `CH:COMPOUND_CLASS`, `CH:FORMULA`, `CH:EXACT_MASS`, `CH:SMILES`, `CH:IUPAC`, `CH:LINK`)
        )
    }
}

object ChemicalGroupParser extends ChemicalGroupParser

trait SampleGroupParser extends FieldParsers {
  def sampleGroup =
    fieldsStartingWith("SP$") ^^ {
      case fields =>
        SampleGroup(
          fields.getValue(`SP:SCIENTIFIC_NAME`),
          fields.getValue(`SP:LINEAGE`),
          fields.getSubtags(`SP:LINK`),
          fields.getValue(`SP:SAMPLE`),
          fields -- List(`SP:SCIENTIFIC_NAME`, `SP:LINEAGE`, `SP:LINK`, `SP:SAMPLE`)
        )
    }
}

object SampleGroupParser extends SampleGroupParser

trait AnalyticalChemistryGroupParser extends FieldParsers {
  def analyticalChemistryGroup =
    fieldsStartingWith("AC$") ^^ {
      case fields =>
        AnalyticalChemistryGroup(
          fields.getValue(`AC:INSTRUMENT`),
          fields.getValue(`AC:INSTRUMENT_TYPE`),
          fields.getSubtags(`AC:MASS_SPECTROMETRY`),
          fields.getSubtagList(`AC:CHROMATOGRAPHY`),
          fields -- List(`AC:INSTRUMENT`, `AC:INSTRUMENT_TYPE`, `AC:MASS_SPECTROMETRY`, `AC:CHROMATOGRAPHY`)
        )
    }
}

object AnalyticalChemistryGroupParser extends AnalyticalChemistryGroupParser

trait MassSpectralDataGroupParser extends FieldParsers {
  def massSpectralDataGroup =
    fieldsStartingWith("MS$") ^^ {
      case fields =>
        MassSpectralDataGroup(
          fields.getSubtags(`MS:FOCUSED_ION`),
          fields.getSubtags(`MS:DATA_PROCESSING`),
          fields -- List(`MS:FOCUSED_ION`, `MS:DATA_PROCESSING`)
        )
    }
}

object MassSpectralDataGroupParser extends MassSpectralDataGroupParser

trait MassSpectralPeakDataGroupParser extends FieldParsers {
  def massSpectralPeakDataGroup =
    fieldsStartingWith("PK$", tag => !List(`PK:ANNOTATION`, `PK:NUM_PEAK`).contains(tag)) ~
      lineWhere(tag => tag.startsWith(`PK:ANNOTATION`) || !tag.startsWith("PK$")).* ~
      fieldsStartingWith("PK$") ~
      peakTriple.* ^^ {
      case pk1 ~ annotation ~ pk2 ~ peaks =>
        val fields = pk1 ++ pk2
        val numPeak = fields.getValue(`PK:NUM_PEAK`).flatMap(s => Try(s.toInt).toOption)
        val others = fields -- List(`PK:SPLASH`)
        val parsedPeaks = peaks.flatten

        require(numPeak.getOrElse(0) == parsedPeaks.length)

        // Remove tag
        val as = annotation.map(s => s.replaceAll("""PK\$ANNOTATION:""", "").trim)

        MassSpectralPeakDataGroup(
          fields.getValue(`PK:SPLASH`),
          as,
          numPeak.getOrElse(0),
          PeakData(parsedPeaks),
          others
        )
    }
}

object MassSpectralPeakDataGroupParser extends MassSpectralPeakDataGroupParser
