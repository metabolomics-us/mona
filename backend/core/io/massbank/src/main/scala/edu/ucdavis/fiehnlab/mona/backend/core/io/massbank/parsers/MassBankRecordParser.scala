package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.types.MassBankRecord

import scala.util.parsing.combinator.JavaTokenParsers

/** Base ADT types and overall parsing structure for the MassBank record format. */
trait MassBankRecordParser extends JavaTokenParsers
  with RecordSpecificGroupParser
  with ChemicalGroupParser
  with SampleGroupParser
  with AnalyticalChemistryGroupParser
  with MassSpectralDataGroupParser
  with MassSpectralPeakDataGroupParser {

  /** The reference specification version. */
  val version = "2.10 (2016-03-01)"

  def massBankRecord: Parser[MassBankRecord] =
    recordSpecificGroup ~
      chemicalGroup ~
      sampleGroup ~
      analyticalChemistryGroup ~
      massSpectralDataGroup ~
      massSpectralPeakDataGroup <~
      "//" ^^ {
      case base ~ ch ~ sp ~ ac ~ ms ~ pk => MassBankRecord(base, ch, sp, ac, ms, pk)
    }
}

object MassBankRecordParser extends MassBankRecordParser
