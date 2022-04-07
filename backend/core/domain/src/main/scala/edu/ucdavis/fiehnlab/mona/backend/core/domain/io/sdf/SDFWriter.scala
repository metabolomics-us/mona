package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.sdf

import java.io.{PrintWriter, Writer}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainWriter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}

/**
  * Created by sajjan on 2/21/18.
  */
class SDFWriter extends DomainWriter{

  override val CRLF: Boolean = true

  /**
    * General function for exporting SDF metadata format
    * @param name
    * @param value
    * @param writer
    */
  private def printMetaData(name: String, value: String, writer: PrintWriter): Unit = {
    writer.println(s">  <$name>")
    writer.println(value)
    writer.println()
  }

  /**
    *
    * @param compound
    * @param writer
    */
  private def buildMOL(compound: Compound, writer: PrintWriter): Unit = {
    if (compound != null && compound.molFile != null && compound.molFile != "") {
      writer.println(compound.molFile.split("M  END").head.replaceAll("\\s+$", ""))
    } else {
      // Use NIST-style entry for records with no MOL data
      if (compound.names.nonEmpty) {
        writer.println(compound.names.head.name)
      } else {
        writer.println("No Name")
      }

      writer.println()
      writer.println("No Structure")
      writer.println("  0  0  0  0  0  0  0  0  0  0  0")
    }
  }

  /**
    * Uses the biological or first compound, sorting the names by score and writes the highest scored name
    * @param compound
    * @param synonyms
    * @param writer
    */
  private def buildNames(compound: Compound, synonyms: Boolean, writer: PrintWriter): Unit = {
    if (compound != null && compound.names.nonEmpty) {
      printMetaData("NAME", compound.names.head.name, writer)

      if (synonyms) {
        printMetaData("SYNONYMS", compound.names.tail.map(_.name).mkString(getNewLine), writer)
      }
    }
  }

  /**
    * Writes a given metadata value by name from given metadata array
    *
    * @param name
    * @param fieldName
    * @param writer
    * @return
    */
  def buildMetaData(metaData: Array[MetaData], name: String, fieldName: String, writer: PrintWriter, transform: String => String = identity): Unit = {
    val metaDataValue: Option[MetaData] = metaData.find(_.name == fieldName)

    // Write a value only if the metadata field is defined
    if (metaDataValue.isDefined) {
      printMetaData(name, transform(metaDataValue.get.value.toString), writer)
    }
  }

  /**
    * Writes InChIKey metadata string if one is present exists
    *
    * @param compound
    * @param writer
    */
  def buildCompoundInchiKey(compound: Compound, writer: PrintWriter): Unit = {

    if (compound.inchiKey != null && compound.inchiKey.nonEmpty) {
      printMetaData("INCHIKEY", compound.inchiKey, writer)

    } else {
      buildMetaData(compound.metaData, "InChIKey", "INCHIKEY", writer)
    }
  }

  def buildCompoundInChI(compound: Compound, writer: PrintWriter): Unit = {
    if (compound.inchi != null && compound.inchiKey.nonEmpty) {
      printMetaData("INCHI", compound.inchi, writer)
    } else {
      buildMetaData(compound.metaData, "InChI", "INCHI", writer)
    }
  }

  /**
    * Writes the comment string containing all available metadata in the format "name=value"
    *
    * @param spectrum
    * @param writer
    * @return
    */
  def buildComments(spectrum: Spectrum, writer: PrintWriter): Unit = {
    val excludedMetaData = Array(
      "inchikey", "total exact mass", "molecular formula",
      "instrument", "instrument type", "precursor type", "precursor m/z", "ms level",
      "ionization mode", "collision energy"
    )

    val commentsString = getAdditionalMetaData(spectrum, excludedMetaData)
      .map(x => s"${x._1}=${x._2.toString}")
      .mkString(getNewLine)

    printMetaData("COMMENT", commentsString, writer)
  }

  /**
    * generates an array of string ion intensity
    *
    * @param spectrum
    * @param writer
    * @return
    */
  def buildSpectraString(spectrum: Spectrum, writer: PrintWriter): Unit = {
    val ions: Array[String] = spectrum.spectrum.split(" ").map(_.split(":").mkString(" "))

    printMetaData("NUM PEAKS", ions.length.toString, writer)
    printMetaData("MASS SPECTRAL PEAKS", ions.mkString(getNewLine), writer)
  }


  /**
    * write the output as a valid NISTMS msp file
    * as defined here
    *
    * http://chemdata.nist.gov/mass-spc/ftp/mass-spc/PepLib.pdf
    *
    * @param spectrum
    * @return
    */
  def write(spectrum: Spectrum, writer: Writer): Unit = {
    val p = getPrintWriter(writer)

    val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

    // MOL data, name and synonyms
    buildMOL(compound, p)
    buildNames(compound, synonyms = true, p)

    // MetaData
    buildMetaData(spectrum.metaData, "PRECURSOR TYPE", "precursor type", p)
    buildMetaData(spectrum.metaData, "SPECTRUM TYPE", "ms level", p)
    buildMetaData(spectrum.metaData, "PRECURSOR M/Z", "precursor m/z", p)
    buildMetaData(spectrum.metaData, "INSTRUMENT TYPE", "instrument type", p)
    buildMetaData(spectrum.metaData, "INSTRUMENT", "instrument", p)
    buildMetaData(spectrum.metaData, "COLLISION ENERGY", "collision energy", p)
    buildMetaData(spectrum.metaData, "ION MODE", "ionization mode", p, x => x.charAt(0).toUpper.toString)
    buildCompoundInchiKey(compound, p)
    buildCompoundInChI(compound, p)
    buildMetaData(compound.metaData, "FORMULA", "molecular formula", p)
    buildMetaData(compound.metaData, "EXACT MASS", "total exact mass", p)
    buildMetaData(compound.metaData, "MW", "total exact mass", p, x => (x.toDouble + 0.2).toInt.toString)
    printMetaData("ID", spectrum.id, p)
    printMetaData("CONTRIBUTOR", s"${spectrum.submitter.firstName} ${spectrum.submitter.lastName}, {${spectrum.submitter.institution}", p)
    buildComments(spectrum, p)
    buildSpectraString(spectrum, p)
    p.println("$$$$")

    p.flush()
  }
}
