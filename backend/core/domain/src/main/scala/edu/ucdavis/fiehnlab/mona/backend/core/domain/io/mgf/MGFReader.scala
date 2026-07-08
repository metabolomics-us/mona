package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.mgf

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.{DomainReadEventHandler, DomainReader}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.upload.{RawMetaEntry, RawParsedSpectrum}

import java.io.Reader
import java.util.regex.Pattern
import scala.util.Try

/**
  * Line-for-line port of angular-mgf-parser's MgfParserLibService.convertWithCallback. Reads the
  * file in bounded chunks, mirroring MSPReader, since MGF blocks (BEGIN IONS .. END IONS) are
  * already explicitly delimited and a natural fit for chunk-boundary carrying
  */
class MGFReader extends DomainReader[RawParsedSpectrum] with LazyLogging {

  private val ChunkSize = 4 * 1024 * 1024
  private val MaxCarrySize = 512 * 1024 * 1024

  private val BlockPattern = Pattern.compile("BEGIN IONS(.*?)END IONS", Pattern.DOTALL)
  private val AttributePattern = Pattern.compile("""\s*([^=\s]+)=(.*)\s""")
  private val PeakPattern = Pattern.compile(
    """^((?:0|[1-9]\d*)(?:\.\d*)?(?:[eE][+\-]?\d+)?)[ \t]((?:0|[1-9]\d*)(?:\.\d*)?(?:[eE][+\-]?\d+)?)[ \t]*(.+)?$""",
    Pattern.MULTILINE
  )
  private val AccurateMassPattern = Pattern.compile("""\d*\.?\d{3,}""")
  private val InchiKeyPattern = Pattern.compile(""".*([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+.*""")
  private val SmilesPattern = Pattern.compile("""^([^J][0-9A-Za-z@+\-\[\]\(\)\\/%=#$,.~&!]{6,})$""")
  private val CommentPattern = Pattern.compile("""(\w+)\s*=\s*([0-9]*\.?[0-9]+)""")
  private val SearchIdPattern = Pattern.compile("""(\w+\s?\w*)+:\s*([\w\d]+[ \w\d-]+)""")
  private val FocusedIonPattern = Pattern.compile("""\s*(.+):(.+)""")
  private val RetentionIndexPattern = Pattern.compile("""(.+)_RI(.*)""")
  private val NameWithInstrumentsPattern = Pattern.compile("""\s*([:\w\d\s-]+);""")

  override def read(input: Reader, handler: DomainReadEventHandler[RawParsedSpectrum]): Unit = {
    val buf = new Array[Char](ChunkSize)
    var carry = ""
    var eof = false

    while (!eof) {
      val n = input.read(buf)
      if (n == -1) {
        eof = true
      } else {
        val chunk = carry + new String(buf, 0, n)
        carry = consumeBlocks(chunk, handler)
        if (carry.length > MaxCarrySize) {
          logger.warn(s"MGF upload never produced a matching block within $MaxCarrySize bytes, aborting")
          eof = true
          carry = ""
        }
      }
    }

    if (carry.nonEmpty) {
      consumeBlocks(carry, handler)
    }
  }

  private def consumeBlocks(text: String, handler: DomainReadEventHandler[RawParsedSpectrum]): String = {
    val matcher = BlockPattern.matcher(text)
    var lastEnd = 0
    while (matcher.find()) {
      lastEnd = matcher.end()
      parseBlock(matcher.group(1)).foreach(handler.readEvent)
    }
    text.substring(lastEnd)
  }

  private def trim(value: String): String = {
    val trimmed = value.trim
    if (trimmed.length >= 2 && trimmed.head == '"' && trimmed.last == '"') {
      trimmed.substring(1, trimmed.length - 1)
    } else {
      trimmed
    }
  }

  private def numLessOrEqualZero(value: String): Boolean = Try(value.trim.toDouble).map(_ <= 0).getOrElse(false)

  private def ignoreField(name: String, value: String): Boolean = {
    if (trim(name).isEmpty || trim(value).isEmpty) {
      true
    } else {
      val lower = name.toLowerCase
      if (lower == "retentiontime" && numLessOrEqualZero(value)) true
      else if (lower == "retentionindex" && numLessOrEqualZero(value)) true
      else if ((lower == "precursormz" || lower == "derivative_mass" || lower == "parent") && numLessOrEqualZero(value)) true
      else if (lower == "formula") true
      else if (lower == "synon") true
      else if (lower == "id") true
      else false
    }
  }

  private def findCategory(rawName: String): String = {
    val name = rawName.toLowerCase
    if (name.isEmpty) {
      "none"
    } else if (name == "retentionindex" || name == "retention index" || name == "retentiontime" || name == "retention time") {
      "spectral properties"
    } else if (name == "instrument" || name == "instrumenttype" || name == "ionmode" || name == "precursormz") {
      "acquisition properties"
    } else {
      "none"
    }
  }

  private def subMetaEntries(value: String, pattern: Pattern, category: String): List[RawMetaEntry] = {
    val matcher = pattern.matcher(value)
    val entries = List.newBuilder[RawMetaEntry]
    while (matcher.find()) {
      val name = trim(matcher.group(1))
      val parsedValue = trim(matcher.group(2))
      if (!ignoreField(name, parsedValue)) {
        entries += RawMetaEntry(name, parsedValue, Some(category))
      }
    }
    entries.result()
  }

  private case class BlockState(
    names: List[String] = Nil,
    name: Option[String] = None,
    meta: List[RawMetaEntry] = Nil,
    inchiKey: Option[String] = None,
    inchi: Option[String] = None,
    smiles: Option[String] = None
  )

  private def handleName(value: String, state: BlockState): BlockState = {
    val riMatcher = RetentionIndexPattern.matcher(value)
    val combinedMatcher = NameWithInstrumentsPattern.matcher(value)
    if (riMatcher.find()) {
      val newName = trim(riMatcher.group(1))
      val retentionIndex = RawMetaEntry("Retention Index", trim(riMatcher.group(2)), Some(findCategory("Retention Index")))
      state.copy(names = state.names :+ newName, meta = state.meta :+ retentionIndex)
    } else if (combinedMatcher.find()) {
      state.copy(names = state.names :+ trim(combinedMatcher.group(1)))
    } else {
      state.copy(name = Some(trim(value)))
    }
  }

  private def inspectFields(name: String, value: String, state: BlockState): BlockState = {
    val inchiKeyMatcher = InchiKeyPattern.matcher(value)
    val lowerName = name.toLowerCase

    if (inchiKeyMatcher.find()) {
      state.copy(inchiKey = Some(inchiKeyMatcher.group(1)))
    } else if (lowerName == "inchi" || lowerName == "inchicode" || lowerName == "inchi code") {
      state.copy(inchi = Some(trim(value)))
    } else if (lowerName == "smiles" && SmilesPattern.matcher(value).find()) {
      val smilesMatcher = SmilesPattern.matcher(value)
      smilesMatcher.find()
      state.copy(smiles = Some(smilesMatcher.group(1)))
    } else if (lowerName == "comment") {
      state.copy(meta = state.meta ++ subMetaEntries(value, CommentPattern, "none"))
    } else if (lowerName == "searchid") {
      state.copy(meta = state.meta ++ subMetaEntries(value, SearchIdPattern, "Database Identifier"))
    } else if (lowerName == "ms$focused_ion") {
      state.copy(meta = state.meta ++ subMetaEntries(value, FocusedIonPattern, "Derivatization"))
    } else if (!ignoreField(name, value)) {
      state.copy(meta = state.meta :+ RawMetaEntry(name, value, Some(findCategory(name))))
    } else {
      state
    }
  }

  // JS Number.toString() picks plain vs exponential notation by a rule not worth replicating
  // exactly here; this normalizes scientific notation mz/intensity tokens to a plain decimal
  // string, which is numerically equivalent even if the literal formatting can differ at the
  // extremes (values further pass through java.util.regex fine either way)
  private def normalizeScientific(value: String): String = {
    if (value.toLowerCase.contains("e")) {
      Try(BigDecimal(value).bigDecimal.stripTrailingZeros.toPlainString).getOrElse(value)
    } else {
      value
    }
  }

  private def parseBlock(block: String): Option[RawParsedSpectrum] = {
    val attrMatcher = AttributePattern.matcher(block)
    var state = BlockState()
    while (attrMatcher.find()) {
      val name = attrMatcher.group(1)
      val value = attrMatcher.group(2)
      state = if (name.equalsIgnoreCase("name")) {
        handleName(value, state)
      } else {
        inspectFields(name, value, state)
      }
    }

    val peakMatcher = PeakPattern.matcher(block)
    val ions = List.newBuilder[String]
    val annotationMeta = List.newBuilder[RawMetaEntry]
    var accurate = true
    while (peakMatcher.find()) {
      val mz = normalizeScientific(peakMatcher.group(1))
      val intensity = normalizeScientific(peakMatcher.group(2))
      ions += s"$mz:$intensity"
      if (!AccurateMassPattern.matcher(mz).find()) {
        accurate = false
      }
      val annotation = peakMatcher.group(3)
      if (annotation != null) {
        annotationMeta += RawMetaEntry(trim(annotation), mz, Some("annotation"))
      }
    }

    val spectrumString = ions.result().mkString(" ")

    if (spectrumString.nonEmpty) {
      Some(RawParsedSpectrum(
        names = state.names.distinct,
        name = state.name,
        meta = state.meta ++ annotationMeta.result(),
        spectrum = spectrumString,
        accurate = accurate,
        inchiKey = state.inchiKey,
        inchi = state.inchi,
        smiles = state.smiles
      ))
    } else {
      logger.warn("invalid spectrum found -> ignored")
      None
    }
  }
}
