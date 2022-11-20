package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.sdf

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{CompoundDAO, MetaDataDAO, Spectrum}

import java.io.{PrintWriter, Writer}
import scala.jdk.CollectionConverters._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainWriter

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
  private def buildMOL(compound: CompoundDAO, writer: PrintWriter): Unit = {
    if (compound != null && compound.getMolFile != null && compound.getMolFile != "") {
      writer.println(compound.getMolFile.split("M  END").head.replaceAll("\\s+$", ""))
    } else {
      // Use NIST-style entry for records with no MOL data
      if (compound.getNames.asScala.nonEmpty) {
        writer.println(compound.getNames.asScala.head.getName)
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
  private def buildNames(compound: CompoundDAO, synonyms: Boolean, writer: PrintWriter): Unit = {
    if (compound != null && compound.getNames.asScala.nonEmpty) {
      printMetaData("NAME", compound.getNames.asScala.head.getName, writer)

      if (synonyms) {
        printMetaData("SYNONYMS", compound.getNames.asScala.tail.map(_.getName).mkString(getNewLine), writer)
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
  def buildMetaData(metaData: Array[MetaDataDAO], name: String, fieldName: String, writer: PrintWriter, transform: String => String = identity): Unit = {
    val metaDataValue: Option[MetaDataDAO] = metaData.find(_.getName == fieldName)

    // Write a value only if the metadata field is defined
    if (metaDataValue.isDefined) {
      printMetaData(name, transform(metaDataValue.get.getValue), writer)
    }
  }

  /**
    * Writes InChIKey metadata string if one is present exists
    *
    * @param compound
    * @param writer
    */
  def buildCompoundInchiKey(compound: CompoundDAO, writer: PrintWriter): Unit = {

    if (compound.getInchiKey != null && compound.getInchiKey.nonEmpty) {
      printMetaData("INCHIKEY", compound.getInchiKey, writer)

    } else {
      buildMetaData(compound.getMetaData.asScala.toArray, "InChIKey", "INCHIKEY", writer)
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
    val ions: Array[String] = spectrum.getSpectrum.split(" ").map(_.split(":").mkString(" "))

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

    val compound: CompoundDAO = spectrum.getCompound.asScala.find(_.getKind == "biological").getOrElse(spectrum.getCompound.asScala.head)

    // MOL data, name and synonyms
    buildMOL(compound, p)
    buildNames(compound, synonyms = true, p)

    // MetaData
    buildMetaData(spectrum.getMetaData.asScala.toArray, "PRECURSOR TYPE", "precursor type", p)
    buildMetaData(spectrum.getMetaData.asScala.toArray, "SPECTRUM TYPE", "ms level", p)
    buildMetaData(spectrum.getMetaData.asScala.toArray, "PRECURSOR M/Z", "precursor m/z", p)
    buildMetaData(spectrum.getMetaData.asScala.toArray, "INSTRUMENT TYPE", "instrument type", p)
    buildMetaData(spectrum.getMetaData.asScala.toArray, "INSTRUMENT", "instrument", p)
    buildMetaData(spectrum.getMetaData.asScala.toArray, "COLLISION ENERGY", "collision energy", p)
    buildMetaData(spectrum.getMetaData.asScala.toArray, "ION MODE", "ionization mode", p, x => x.charAt(0).toUpper.toString)
    buildCompoundInchiKey(compound, p)
    buildMetaData(compound.getMetaData.asScala.toArray, "FORMULA", "molecular formula", p)
    buildMetaData(compound.getMetaData.asScala.toArray, "EXACT MASS", "total exact mass", p)
    buildMetaData(compound.getMetaData.asScala.toArray, "MW", "total exact mass", p, x => (x.toDouble + 0.2).toInt.toString)
    printMetaData("ID", spectrum.getId, p)
    printMetaData("CONTRIBUTOR", s"${spectrum.getSubmitter.getFirstName} ${spectrum.getSubmitter.getLastName}, {${spectrum.getSubmitter.getInstitution}", p)
    buildComments(spectrum, p)
    buildSpectraString(spectrum, p)
    p.println("$$$$")

    p.flush()
  }
}
