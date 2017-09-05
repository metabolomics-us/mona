package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp

import java.io.{PrintWriter, Writer}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainWriter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}

/**
  * Created by wohlgemuth on 5/27/16.
  */
class MSPWriter extends DomainWriter {

  /**
    * Uses the biological or first compound, sorting the names by score and writes the highest scored name
    *
    * @param spectrum
    * @param writer
    * @return
    */
  def buildNames(spectrum: Spectrum, synonyms: Boolean, writer: PrintWriter): Unit = {
    val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

    if (compound != null && compound.names.nonEmpty) {
      // TODO Re-enable sorting by score when implemented
      // val names = compound.head.names.sortBy(_.score).headOption.orNull

      writer.println(s"Name: ${compound.names.head.name}")

      if (synonyms) {
        compound.names.tail.foreach(name => writer.println(s"Synon: ${name.name}"))
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
  def buildCompoundMetaData(spectrum: Spectrum, name: String, fieldName: String, writer: PrintWriter, transform: String => String = identity): Unit = {
    val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

    if (compound != null) {
      buildMetaData(compound.metaData, name, fieldName, writer, transform)
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
      writer.println(s"$name: ${transform(metaDataValue.get.value.toString)}")
    }
}

  /**
    * Writes InChIKey metadata string if one is present exists
    *
    * @param spectrum
    * @param writer
    */
  def buildCompoundInchiKey(spectrum: Spectrum, writer: PrintWriter): Unit = {
    val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

    if (compound != null) {
      if (compound.inchiKey != null && compound.inchiKey.nonEmpty) {
        writer.println(s"InChIKey: ${compound.inchiKey}")
      }

      else {
        val metaData: Option[MetaData] = compound.metaData.find(_.name == "InChIKey")

        if (metaData.isDefined) {
          writer.println(s"InChIKey: ${metaData.get.value}")
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
  def buildComments(spectrum: Spectrum, writer: PrintWriter): Unit = {
    val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

    val comments = (spectrum.metaData ++ compound.metaData).map(
      value => s""""${value.name}=${value.value.toString.replaceAll("\"", "")}""""
    ).mkString(" ")

    writer.println(s"Comments: $comments")
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
  def write(spectrum: Spectrum, writer: Writer): Unit = {
    val p = new PrintWriter(writer)

    // Name and synonyms, including NIST-specific fields
    buildNames(spectrum, synonyms = true, p)
    p.println("SYNON: $:00in-source")

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
    p.println(s"DB#: ${spectrum.id}")
    buildCompoundInchiKey(spectrum, p)
    buildMetaData(spectrum.metaData, "Precursor_type", "precursor type", p)
    buildMetaData(spectrum.metaData, "Spectrum_type", "ms level", p)
    buildMetaData(spectrum.metaData, "PrecursorMZ", "precursor m/z", p)
    buildMetaData(spectrum.metaData, "Instrument_type", "instrument type", p)
    buildMetaData(spectrum.metaData, "Instrument", "instrument", p)
    buildMetaData(spectrum.metaData, "Ionization", "ionization mode", p, x => x.charAt(0).toUpper.toString)
    buildMetaData(spectrum.metaData, "Collision_energy", "collision energy", p)
    buildCompoundMetaData(spectrum, "InChIKey", "InChIKey", p)
    buildCompoundMetaData(spectrum, "Retention_index", "retention index", p)
    buildCompoundMetaData(spectrum, "Formula", "molecular formula", p)
    buildCompoundMetaData(spectrum, "MW", "total exact mass", p, x => (x.toDouble + 0.2).toInt.toString)
    buildCompoundMetaData(spectrum, "ExactMass", "total exact mass", p)
    buildComments(spectrum, p)
    buildSpectraString(spectrum, p)

    p.println()
    p.flush()
  }
}
