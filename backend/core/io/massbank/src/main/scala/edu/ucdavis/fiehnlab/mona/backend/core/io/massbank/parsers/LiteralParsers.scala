package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import java.time.LocalDate

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.types._

import scala.util.parsing.combinator.JavaTokenParsers

/** Parsers for additional literals in MassBank */
trait LiteralParsers extends JavaTokenParsers {
  def tag: Parser[String] = """[\w-_\$]+""".r

  def anyString: Parser[String] = ".+".r ^^ (_.trim)

  def integer: Parser[Int] = wholeNumber ^^ (_.toInt)

  def double: Parser[Double] = floatingPointNumber ^^ (_.toDouble)

  def date: Parser[LocalDate] = "[\\d]{4,}".r ~ "." ~ "[\\d]{1,2}".r ~ "." ~ "[\\d]{1,2}".r ^^ {
    case year ~ "." ~ month ~ "." ~ day => LocalDate.of(year.toInt, month.toInt, day.toInt)
  }

  def subtag: Parser[(String, String)] = """[\w][\w\d/_-]*""".r ~ anyString ^^ { case subtag ~ value => (subtag, value) }

  def peakTriple: Parser[Option[PeakTriple]] =
    (double ~ double ~ double ^^ { case a ~ b ~ c => Some(PeakTriple(a, b, c)) }) |
      "N/A" ^^ (_ => None)
}

object LiteralParsers extends LiteralParsers
