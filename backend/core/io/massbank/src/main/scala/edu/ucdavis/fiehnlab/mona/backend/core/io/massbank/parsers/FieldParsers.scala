package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import scala.util.Try

trait FieldParsers extends LiteralParsers {

  /** Special field for PK$NUM_PEAK to handle non-integral (e.g. N/A) values */
  def numPeakField(name: String): Parser[Option[Int]] = name ~> ":" ~> anyString ^^ {
    case value => Try(value.toInt).toOption
  }

  /** Take all fields where the field tag starts with a string and satisfies a given predicate */
  def fieldStartingWith(prefix: String, predicate: String => Boolean): Parser[(String, String)] =
    tag ~ ":" ~ anyString ^? {
      case name ~ ":" ~ value if predicate(name) && name.startsWith(prefix) => name -> value
    }

  def lineWhere(predicate: String => Boolean): Parser[String] = anyString ^? {
    case value if predicate(value) => value
  }

  /** Creates a map of values, where collisions are handled using separate chaining */
  def collapseToMap(l: List[(String, String)]): Map[String, List[String]] = l.groupBy(_._1).map { case (k, v) => k -> v.map(_._2) }

  def fieldsStartingWith(prefix: String, predicate: String => Boolean = _ => true): Parser[Map[String, List[String]]] =
    fieldStartingWith(prefix, predicate).* ^^ collapseToMap

  /** Helper functions to quickly extract field values */
  implicit class FieldMapExtensions(fields: Map[String, List[String]]) extends FieldParsers {
    def getValue(key: String): Option[String] = fields.get(key).flatMap(_.headOption)

    def getIterative(key: String): List[String] = fields.getOrElse(key, List.empty)

    def getSubtags(key: String): Map[String, String] = {
      val subtags = fields.getOrElse(key, List.empty)
      val results = subtags.map(f => parse(subtag, f)).filter(_.successful).map(_.get)
      results.toMap
    }

    def getSubtagList(key: String): Map[String, List[String]] = {
      val subtags = fields.getOrElse(key, List.empty)
      val results = subtags.map(f => parse(subtag, f)).filter(_.successful).map(_.get)
      results.groupBy(_._1).map { case (k, v) => (k -> v.map(_._2)) }
    }

  }

}

object FieldParsers extends FieldParsers
