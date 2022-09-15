package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{CompoundDAO, MetaDataDAO}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainWriter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult

import java.io.{PrintWriter, Writer}
import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 5/27/16.
  */
class MSPWriter extends DomainWriter {

  override val CRLF: Boolean = true

  /**
    * Uses the biological or first compound, sorting the names by score and writes the highest scored name
    *
    * @param spectrum
    * @param writer
    * @return
    */
  def buildNames(spectrum: SpectrumResult, synonyms: Boolean, writer: PrintWriter): Unit = {
    val compound: CompoundDAO = spectrum.getSpectrum.getCompound.asScala.find(_.getKind == "biological").getOrElse(spectrum.getSpectrum.getCompound.asScala.head)

    if (compound != null && compound.getNames.asScala.nonEmpty) {
      // TODO Re-enable sorting by score when implemented
      // val names = compound.head.names.sortBy(_.score).headOption.orNull

      writer.println(s"Name: ${compound.getNames.asScala.head.getName}")

      if (synonyms) {
        compound.getNames.asScala.tail.foreach(name => writer.println(s"Synon: ${name.getName}"))
      }
    } else {
      writer.println("Name: None")
    }
  }


  /**
    * Writes a given metadata value by name from the biological or first compound
    *
    * @param spectrum
    * @param name
    * @param fieldName
    * @param writer
    * @return
    */
  def buildCompoundMetaData(spectrum: SpectrumResult, name: String, fieldName: String, writer: PrintWriter, transform: String => String = identity): Unit = {
    val compound: CompoundDAO = spectrum.getSpectrum.getCompound.asScala.find(_.getKind == "biological").getOrElse(spectrum.getSpectrum.getCompound.asScala.head)

    if (compound != null) {
      buildMetaData(compound.getMetaData.asScala.toArray, name, fieldName, writer, transform)
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
      writer.println(s"$name: ${transform(metaDataValue.get.getValue)}")
    }
  }

  /**
    * Writes InChIKey metadata string if one is present exists
    *
    * @param spectrum
    * @param writer
    */
  def buildCompoundInchiKey(spectrum: SpectrumResult, writer: PrintWriter): Unit = {
    val compound: CompoundDAO = spectrum.getSpectrum.getCompound.asScala.find(_.getKind == "biological").getOrElse(spectrum.getSpectrum.getCompound.asScala.head)

    if (compound != null) {
      if (compound.getInchiKey != null && compound.getInchiKey.nonEmpty) {
        writer.println(s"InChIKey: ${compound.getInchiKey}")
      }

      else {
        val metaData: Option[MetaDataDAO] = compound.getMetaData.asScala.find(_.getName == "InChIKey")

        if (metaData.isDefined) {
          writer.println(s"InChIKey: ${metaData.get.getValue}")
        }
      }
    }
  }

  /**
   * Writes InChI metadata string if one is present exists
   *
   * @param spectrum
   * @param writer
   */
  def buildCompoundInChi(spectrum: SpectrumResult, writer: PrintWriter): Unit = {
    val compound: CompoundDAO = spectrum.getSpectrum.getCompound.asScala.find(_.getKind == "biological").getOrElse(spectrum.getSpectrum.getCompound.asScala.head)

    if (compound != null) {
      if(compound.getInchi != null && compound.getInchi.nonEmpty) {
        writer.println(s"InChI: ${compound.getInchi}")
      }
      else {
        val metaData: Option[MetaDataDAO] = compound.getMetaData.asScala.find(_.getName == "InChI")

        if (metaData.isDefined) {
          writer.println(s"InChI: ${metaData.get.getValue}")
        }
      }
    }
  }
  /**
    * Writes the comment string containing all available metadata in the format "name=value"
    *
    * @param spectrum
    * @param writer
    * @return
    */
  def buildComments(spectrum: SpectrumResult, writer: PrintWriter): Unit = {
    val excludedMetaData = Array(
      "inchikey", "total exact mass", "molecular formula",
      "instrument", "instrument type", "precursor type", "precursor m/z", "ms level",
      "ionization mode", "collision energy", "retention index"
    )

    val commentsString = getAdditionalMetaData(spectrum, excludedMetaData)
      .map(x => s""""${x._1}=${x._2.toString}"""").mkString(" ")

    writer.println(s"Comments: $commentsString")
  }

  /**
    * generates an array of string ion intensity
    *
    * @param spectrum
    * @param writer
    * @return
    */
  def buildSpectraString(spectrum: SpectrumResult, writer: PrintWriter): Unit = {
    val ions: Array[String] = spectrum.getSpectrum.getSpectrum.split(" ").map(_.split(":").mkString(" "))

    writer.println(s"Num Peaks: ${ions.length}")
    ions.foreach(writer.println)
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
  def write(spectrum: SpectrumResult, writer: Writer): Unit = {
    val p = getPrintWriter(writer)

    // Name and synonyms, including NIST-specific fields
    buildNames(spectrum, synonyms = true, p)
    p.println("Synon: $:00in-source")

    // TODO properly handle additional NIST fields
    //   $:04 - blank
    //   $:05 - ??
    //   $:06 - MS type/instrument type
    //   $:07 - instrument make/model
    //   $:09 - chromatogram make/model
    //   $:10 - ionization method (EI, ESI, etc)
    //   $:11 - ionization mode (P or N)
    //   $:12 - collision gas
    //   $:14 - MS scan range
    //   $:16 - voltage
    // https://github.com/cbroeckl/RAMClustR/blob/master/R/ramclustR2.R

    // MetaData
    p.println(s"DB#: ${spectrum.getSpectrum.getId}")
    buildCompoundInchiKey(spectrum, p)
    buildCompoundInChi(spectrum, p)
    buildMetaData(spectrum.getSpectrum.getMetaData.asScala.toArray, "Precursor_type", "precursor type", p)
    buildMetaData(spectrum.getSpectrum.getMetaData.asScala.toArray, "Spectrum_type", "ms level", p)
    buildMetaData(spectrum.getSpectrum.getMetaData.asScala.toArray, "PrecursorMZ", "precursor m/z", p)
    buildMetaData(spectrum.getSpectrum.getMetaData.asScala.toArray, "Instrument_type", "instrument type", p)
    buildMetaData(spectrum.getSpectrum.getMetaData.asScala.toArray, "Instrument", "instrument", p)
    buildMetaData(spectrum.getSpectrum.getMetaData.asScala.toArray, "Ion_mode", "ionization mode", p, x => x.charAt(0).toUpper.toString)
    buildMetaData(spectrum.getSpectrum.getMetaData.asScala.toArray, "Collision_energy", "collision energy", p)
    buildMetaData(spectrum.getSpectrum.getMetaData.asScala.toArray, "Retention_index", "retention index", p)
    buildCompoundMetaData(spectrum, "Formula", "molecular formula", p)
    buildCompoundMetaData(spectrum, "MW", "total exact mass", p, x => (x.toDouble + 0.2).toInt.toString)
    buildCompoundMetaData(spectrum, "ExactMass", "total exact mass", p)
    buildComments(spectrum, p)
    buildSpectraString(spectrum, p)

    p.println()
    p.flush()
  }
}
