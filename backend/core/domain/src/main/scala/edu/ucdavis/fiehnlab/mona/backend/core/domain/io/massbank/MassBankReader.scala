package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.massbank

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.{DomainReadEventHandler, DomainReader}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.upload.{RawMetaEntry, RawParsedSpectrum}

import java.io.Reader
import java.util.regex.Pattern
import scala.util.Try

/**
  * Port of angular-massbank-parser's MassbankParserLibService.convertWithCallback. Follows the
  * MassBank Record Format v2.09. Unlike MSP/MGF, a .txt record is exactly one spectrum per file
  * by format convention, and these single-record files are small, so this reads the whole file
  * into memory rather than streaming in chunks - that is intentional, not an oversight
  */
class MassBankReader extends DomainReader[RawParsedSpectrum] with LazyLogging {

  private val AttributePattern = Pattern.compile("""\s*(\S+):\s(.+)\s""")
  private val AnnotationPattern = Pattern.compile("""\s\s(\d+\.?\d*)(?:\s\d+)?\s+.*(\[.+\][+\-]?(?:\(.+\))?).*""")
  private val AddMetaDataSubPattern = Pattern.compile("""(\w+[/]*\w)\s(.+)""")
  private val RetentionTimeUnitPattern = Pattern.compile("""([0-9]+\.?[0-9]+).*min.*""")
  private val PrecursorMzPattern = Pattern.compile("""([0-9]+\.?[0-9]+)""")
  private val DollarPrefixPattern = Pattern.compile("""^(?:[a-zA-Z\s])*\$(.*)$""", Pattern.CASE_INSENSITIVE)
  private val PeakPattern = Pattern.compile(
    """\s\s((?:0|[1-9]\d*)(?:\.\d*)?(?:[eE][+\-]?\d+)?)\s((?:0|[1-9]\d*)(?:\.\d*)?(?:[eE][+\-]?\d+)?)\s(\d+)\b"""
  )
  private val AccurateMassPattern = Pattern.compile("""[0-9]*\.?[0-9]{3,}""")

  override def read(input: Reader, handler: DomainReadEventHandler[RawParsedSpectrum]): Unit = {
    val content = new java.io.BufferedReader(input).lines().toArray.mkString("\n")
    parse(content).foreach(handler.readEvent)
  }

  private def trim(value: String): String = value.trim

  /** Strips a leading "WORD$" (or "$"-only) prefix, case insensitively, mirroring the post
    * processing pass applied to every collected meta name/category */
  private def stripDollarPrefix(value: String): String = {
    val matcher = DollarPrefixPattern.matcher(value)
    if (matcher.matches()) matcher.group(1) else value
  }

  // Faithful port of addMetaData
  private def addMetaData(value: String, category: Option[String], metaAcc: List[RawMetaEntry]): List[RawMetaEntry] = {
    val subMatcher = AddMetaDataSubPattern.matcher(value)
    if (!subMatcher.find()) {
      metaAcc
    } else {
      val key = trim(subMatcher.group(1).toLowerCase)
      val rawValue = subMatcher.group(2)
      val effectiveCategory = category.map(trim).map(c => if (c == "FOCUSED_ION") "MASS_SPECTROMETRY" else c)

      var acc = metaAcc

      if (key == "retention_time") {
        val unitMatcher = RetentionTimeUnitPattern.matcher(rawValue)
        acc = if (unitMatcher.find()) {
          acc :+ RawMetaEntry(key, trim(unitMatcher.group(1)), effectiveCategory)
        } else {
          acc :+ RawMetaEntry(key, trim(rawValue), effectiveCategory)
        }
      }

      if (key == "precursor_m/z") {
        val mzMatcher = PrecursorMzPattern.matcher(rawValue)
        while (mzMatcher.find()) {
          acc = acc :+ RawMetaEntry("precursor m/z", trim(mzMatcher.group(1)), effectiveCategory)
        }
      } else {
        acc = acc :+ RawMetaEntry(key, trim(rawValue), effectiveCategory)
      }

      acc
    }
  }

  private case class State(
    names: List[String] = Nil,
    meta: List[RawMetaEntry] = Nil,
    inchi: Option[String] = None,
    accurate: Boolean = true
  )

  private def parse(buf: String): Option[RawParsedSpectrum] = {
    var state = State()
    val attrMatcher = AttributePattern.matcher(buf)

    while (attrMatcher.find()) {
      val key = attrMatcher.group(1)
      val value = attrMatcher.group(2)

      key match {
        case "PK$PEAK" | "PK$NUM_PEAK" | "CH$SMILES" | "CH$FORMULA" | "RECORD_TITLE" | "DATE" =>
        // skip

        case "CH$NAME" =>
          state = state.copy(names = state.names :+ trim(value))

        case "PK$ANNOTATION" =>
          val annotationMatcher = AnnotationPattern.matcher(buf)
          while (annotationMatcher.find()) {
            state = state.copy(meta = state.meta :+ RawMetaEntry(trim(annotationMatcher.group(2)), trim(annotationMatcher.group(1)), Some("annotation")))
          }

        case "CH$IUPAC" | "CH$INCHI" =>
          val trimmed = trim(value)
          val firstIdx = trimmed.indexOf("InChI=")
          if (firstIdx > -1) {
            val secondIdx = trimmed.indexOf("InChI=", firstIdx + 1)
            state = if (secondIdx > -1) {
              state.copy(inchi = Some(trim(trimmed.substring(secondIdx))))
            } else {
              state.copy(inchi = Some(trimmed))
            }
          } else {
            state = state.copy(names = state.names :+ trimmed)
          }

        case "COMMENT" =>
        // comments are not carried onto the persisted Spectrum by the interactive upload path
        // either (they only apply to the manual review wizard's additionalData), so this is
        // intentionally a no-op here

        case other =>
          if (other.contains("LINK") || other == "AC$MASS_SPECTROMETRY" || other == "AC$CHROMATOGRAPHY" ||
              other == "MS$FOCUSED_ION" || other == "MS$DATA_PROESSING") {
            state = state.copy(meta = addMetaData(value, Some(other), state.meta))
          } else {
            state = state.copy(meta = state.meta :+ RawMetaEntry(trim(other), trim(value)))
          }
      }
    }

    val normalizedMeta = state.meta.map { entry =>
      entry.copy(
        name = stripDollarPrefix(entry.name),
        category = entry.category.map(stripDollarPrefix)
      )
    }

    val peakMatcher = PeakPattern.matcher(buf)
    val triples = List.newBuilder[(String, String, String)]
    var isAbsolute = false
    var accurate = state.accurate
    while (peakMatcher.find()) {
      val mz = peakMatcher.group(1)
      val absoluteIntensity = peakMatcher.group(2)
      val relativeIntensity = peakMatcher.group(3)
      triples += ((mz, absoluteIntensity, relativeIntensity))
      if (Try(absoluteIntensity.toDouble).getOrElse(0d) > 0) {
        isAbsolute = true
      }
      if (!AccurateMassPattern.matcher(mz).find()) {
        accurate = false
      }
    }

    val ions = triples.result().map { case (mz, absolute, relative) =>
      if (isAbsolute) s"$mz:$absolute" else s"$mz:$relative"
    }

    if (ions.nonEmpty && state.names.nonEmpty) {
      Some(RawParsedSpectrum(
        names = state.names,
        meta = normalizedMeta,
        spectrum = ions.mkString(" "),
        accurate = accurate,
        inchi = state.inchi
      ))
    } else {
      logger.warn(s"was not able to find valid spectra for record")
      None
    }
  }
}
