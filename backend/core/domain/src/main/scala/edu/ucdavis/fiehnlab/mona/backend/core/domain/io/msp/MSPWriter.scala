package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp

import java.io.{PrintWriter, Writer}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainWriter

/**
  * Created by wohlgemuth on 5/27/16.
  */
class MSPWriter extends DomainWriter {

  /**
    * Uses the biological or first compound, sorting the names by score and return the highest scored name
    *
    * @param spectrum
    * @return
    */
  def buildName(spectrum: Spectrum): String = {
    val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

    if (compound != null) {
      // TODO Re-enable sorting by score when implemented
      // val names = compound.head.names.sortBy(_.score).headOption.orNull
      val names = compound.names.find(!_.computed).orNull

      if (names == null) {
        "None"
      } else {
        names.name
      }
    } else {
      "No name provided"
    }
  }

  def buildSynonyms(spectrum: Spectrum): Seq[String] = {
    val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

    if (compound != null) {
      compound.names.filter(!_.computed).map { name =>
        s"Synonym: ${name.name}"
      }
    } else {
      Seq.empty[String]
    }
  }

  /**
    * finding the mol weight for the biological compound
    *
    * @param spectrum
    * @return
    */
  def buildCompoundMetaData(spectrum: Spectrum, value: String): String = {
    val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

    if (compound != null) {
      val meta = compound.metaData.find(_.name == value).orNull

      if (meta == null) {
        "0"
      } else {
        meta.value.toString
      }
    } else {
      "0"
    }
  }

  def buildCompoundInchiKey(spectrum: Spectrum): String = {
    val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

    if (compound != null) {
      if (compound.inchiKey != null && compound.inchiKey != "") {
        s"InChIKey: ${compound.inchiKey}"
      } else if (compound.metaData.exists(_.name == "InChIKey")) {
        s"InChIKey: ${compound.metaData.filter(_.name == "InChIKey").head.value.toString}"
      } else {
        ""
      }
    } else {
      ""
    }
  }

  /**
    * builds teh comment string in the format "name=value"
    *
    * @param spectrum
    * @return
    */
  def buildComments(spectrum: Spectrum): String = {
    val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

    (spectrum.metaData ++ compound.metaData).map(
      value => s""""${value.name}=${value.value.toString.replaceAll("\"", "")}""""
    ).mkString(" ")
  }

  /**
    * generates an aray of string ion intensity
    *
    * @param spectrum
    * @return
    */
  def buildSpectraString(spectrum: Spectrum): Array[String] = spectrum.spectrum.split(" ").collect {
    case ionIntensity: String =>
      val pair = ionIntensity.split(":")
      val ion = pair(0)
      val intensity = pair(1)

      s"$ion $intensity"
  }

  def buildMetaDateField(spectrum: Spectrum, field: String): String = {
    val meta = spectrum.metaData.find(_.name == field).orNull
    if (meta == null) {
      "0"
    } else {
      meta.value.toString
    }
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

    p.println(s"Name: ${buildName(spectrum)}")

    buildSynonyms(spectrum).foreach(p.println)

    p.println(s"DB#: ${spectrum.id}")

    val inchikey: String = buildCompoundInchiKey(spectrum)

    if (inchikey.nonEmpty) {
      p.println(inchikey)
    }

    p.println(s"MW: ${buildCompoundMetaData(spectrum, "total exact mass")}")

    p.println(s"Formula: ${buildCompoundMetaData(spectrum, "molecular formula")}")
    p.println(s"PrecursorMZ: ${buildMetaDateField(spectrum, "precursor m/z")}")
    p.println(s"Comments: ${buildComments(spectrum)}")

    val spectrumString = buildSpectraString(spectrum)

    p.println(s"Num Peaks: ${spectrumString.length}")
    spectrumString.foreach(value => p.println(s"$value"))

    p.println()
    p.flush()
  }
}
