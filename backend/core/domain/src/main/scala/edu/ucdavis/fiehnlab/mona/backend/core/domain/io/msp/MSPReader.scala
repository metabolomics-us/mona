package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.{DomainReadEventHandler, DomainReader}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.upload.{RawMetaEntry, RawParsedSpectrum}

import java.io.{BufferedReader, Reader}
import java.util.regex.Pattern

/**
  * Line-for-line port of angular-msp-parser's MspParserLibService.convertWithCallback, with one
  * deliberate departure: block splitting is done by scanning lines rather than by running the
  * original single regex (would cause stack overflow in the regex engine)
  */
class MSPReader extends DomainReader[RawParsedSpectrum] with LazyLogging {

  private val PeakLinePattern = Pattern.compile("""\s*[0-9]*\.?[0-9]+\s+[0-9]*\.?[0-9]+[;]?.*""")
  private val AttributePattern = Pattern.compile("""\s*([a-zA-Z _$/]+):(.+)\s""")
  private val PeakPattern = Pattern.compile(
    """([0-9]+\.?[0-9]*)[ \t]+([0-9]*\.?[0-9]+)(?:\s*(?:[;\n])|(?:"?(.+)"?\n?))?"""
  )
  private val AccurateMassPattern = Pattern.compile("""[0-9]*\.?[0-9]{3,}""")
  private val InchiKeyPattern = Pattern.compile(""".*([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+.*""")
  private val SmilesPattern = Pattern.compile("""^([^J][0-9A-Za-z@+\-\[\]\(\)\\/%=#$,.~&!]{6,})$""")
  private val CommentPattern = Pattern.compile("""(\w+)\s*=\s*([0-9]*\.?[0-9]+)""")
  private val SearchIdPattern = Pattern.compile("""(\w+\s?\w*)+:\s*([\w\d]+[ \w\d-]+)""")
  private val FocusedIonPattern = Pattern.compile("""\s*(.+):(.+)""")
  private val RetentionIndexPattern = Pattern.compile("""(.+)_RI(.*)""")

  private def isAttributeLine(line: String): Boolean = line.contains(":")

  private def isPeakLine(line: String): Boolean = PeakLinePattern.matcher(line).matches()

  override def read(input: Reader, handler: DomainReadEventHandler[RawParsedSpectrum]): Unit = {
    val reader = new BufferedReader(input)
    val metaLines = scala.collection.mutable.ListBuffer[String]()
    val peakLines = scala.collection.mutable.ListBuffer[String]()
    var inPeaks = false

    def flush(): Unit = {
      if (metaLines.nonEmpty || peakLines.nonEmpty) {
        // Trailing "\n" after every line (including the last) since AttributePattern requires a
        // whitespace character after each field's value
        val metaBlock = if (metaLines.isEmpty) "" else metaLines.mkString("", "\n", "\n")
        val peakBlock = if (peakLines.isEmpty) "" else peakLines.mkString("", "\n", "\n")
        parseBlock(metaBlock, peakBlock).foreach(handler.readEvent)
      }
      metaLines.clear()
      peakLines.clear()
      inPeaks = false
    }

    var line = reader.readLine()
    while (line != null) {
      if (inPeaks) {
        if (isPeakLine(line)) {
          peakLines += line
        } else {
          flush()
          if (isAttributeLine(line)) {
            metaLines += line
          }
        }
      } else {
        if (isAttributeLine(line)) {
          metaLines += line
        } else if (isPeakLine(line) && metaLines.nonEmpty) {
          peakLines += line
          inPeaks = true
        }
      }
      line = reader.readLine()
    }
    flush()
  }

  private def trim(value: String): String = {
    val trimmed = value.trim
    if (trimmed.length >= 2 && trimmed.head == '"' && trimmed.last == '"') {
      trimmed.substring(1, trimmed.length - 1)
    } else {
      trimmed
    }
  }

  private def ignoreField(name: String, value: String): Boolean = {
    if (value.isEmpty) {
      true
    } else {
      val lower = name.toLowerCase
      lower == "num peaks" || lower == "numpeaks"
    }
  }

  private def findCategory(rawName: String): String = {
    val name = rawName.toLowerCase
    if (name.isEmpty) {
      "none"
    } else if (name == "num peaks" || name == "retentionindex" || name == "retentiontime") {
      "spectral properties"
    } else if (name == "instrument" || name == "instrumenttype" || name == "ionmode" || name == "precursormz") {
      "acquisition properties"
    } else {
      "none"
    }
  }

  /** Repeats a sub-pattern against a field value, emitting one meta entry per match, mirroring
    * handleMetaDataField's while(match != null) loop */
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
    meta: List[RawMetaEntry] = Nil,
    inchiKey: Option[String] = None,
    inchi: Option[String] = None,
    smiles: Option[String] = None
  )

  private def handleName(value: String, state: BlockState): BlockState = {
    val riMatcher = RetentionIndexPattern.matcher(value)
    if (riMatcher.find()) {
      val newName = trim(riMatcher.group(1))
      val retentionIndex = RawMetaEntry("Retention Index", trim(riMatcher.group(2)), Some(findCategory("Retention Index")))
      state.copy(names = state.names :+ newName, meta = state.meta :+ retentionIndex)
    } else {
      state.copy(names = state.names :+ trim(value))
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

  private def parseBlock(metaBlock: String, peakBlock: String): Option[RawParsedSpectrum] = {
    val attrMatcher = AttributePattern.matcher(metaBlock)
    var state = BlockState()
    while (attrMatcher.find()) {
      val name = trim(attrMatcher.group(1))
      val value = trim(attrMatcher.group(2))
      state = if (name.equalsIgnoreCase("name") || name.equalsIgnoreCase("synon")) {
        handleName(value, state)
      } else {
        inspectFields(name, value, state)
      }
    }

    val uniqueNames = state.names.distinct

    val peakMatcher = PeakPattern.matcher(peakBlock)
    val peaks = List.newBuilder[String]
    val annotationMeta = List.newBuilder[RawMetaEntry]
    var accurate = true
    while (peakMatcher.find()) {
      val mz = peakMatcher.group(1)
      val intensity = peakMatcher.group(2)
      peaks += s"$mz:$intensity"
      if (!AccurateMassPattern.matcher(mz).find()) {
        accurate = false
      }
      val annotation = peakMatcher.group(3)
      if (annotation != null) {
        val cleaned = trim(annotation).replaceAll("(^\"|\"$)", "")
        annotationMeta += RawMetaEntry(cleaned, mz, Some("annotation"))
      }
    }

    val spectrumString = trim(peaks.result().mkString(" "))

    if (spectrumString.nonEmpty && uniqueNames.nonEmpty) {
      Some(RawParsedSpectrum(
        names = uniqueNames,
        meta = state.meta ++ annotationMeta.result(),
        spectrum = spectrumString,
        accurate = accurate,
        inchiKey = state.inchiKey,
        inchi = state.inchi,
        smiles = state.smiles
      ))
    } else {
      logger.warn(s"$uniqueNames is an invalid spectra -> ignored")
      None
    }
  }
}
