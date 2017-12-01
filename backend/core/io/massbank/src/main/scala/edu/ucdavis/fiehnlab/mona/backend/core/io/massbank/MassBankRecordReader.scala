package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers.MassBankRecordParser
import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.types.MassBankRecord

import scala.io.Source
import scala.util.Try

class MassBankRecordParsingException(val message: String) extends IllegalArgumentException(message)

object MassBankRecordReader extends MassBankRecordParser {
  def read(input: String): Try[MassBankRecord] = parseAll(massBankRecord, input) match {
    case Success(record, _) => util.Success(record)
    case NoSuccess(error, x) => util.Failure(new MassBankRecordParsingException(error + " " + x.toString))
  }

  def read(src: Source): Try[MassBankRecord] = {
    val lines = src.getLines.mkString("\n")
    read(lines)
  }
}
