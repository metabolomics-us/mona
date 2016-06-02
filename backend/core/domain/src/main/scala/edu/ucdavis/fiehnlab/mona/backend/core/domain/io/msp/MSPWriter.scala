package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp

import java.io.{PrintWriter, Writer}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainWriter

/**
  * Created by wohlgemuth on 5/27/16.
  */
class MSPWriter extends DomainWriter {

  /**
    * uses the first compound, sorting the names by score and return the highest scored name
    *
    * @param spectrum
    * @return
    */
  def buildName(spectrum: Spectrum): String = {
    val compound = spectrum.compound.find(_.kind == "biological").get

    if (compound != null) {
      val names = compound.names.sortBy(_.score).headOption.get

      if (names == null) {
        "None"
      }
      else {
        names.name
      }
    }
    else {
      "None provided for biological compound"
    }
  }

  /**
    * finding the mol weight for the biological compound
    *
    * @param spectrum
    * @return
    */
  def buildCompoundMetaData(spectrum: Spectrum, value: String): String = {
    if (spectrum.compound.filter(_.kind == "biological") != null) {
      val meta = spectrum.compound.filter(_.kind == "biological").head.metaData.find(_.name == value).get
      if (meta == null) {
        "0"
      }
      else {
        meta.value.toString
      }
    }
    else {
      "0"
    }
  }

  /**
    * builds teh comment string in the format "name=value"
    *
    * @param spectrum
    * @return
    */
  def buildComments(spectrum: Spectrum): String = {
    spectrum.metaData.collect {
      case value: MetaData =>
        s""""${value.name}=${value.value}""""
    }.mkString(" ")
  }

  /**
    * generates an aray of string ion\tintensity
    *
    * @param spectrum
    * @return
    */
  def buildSpectraString(spectrum: Spectrum): Array[String] = spectrum.spectrum.split(" ").collect {
    case ionIntensity: String =>
      val pair = ionIntensity.split(":")
      val ion = pair(0)
      val intensity = pair(1)

      s"${ion}\t${intensity}"
  }

  def buildMetaDateField(spectrum: Spectrum, field: String): String = {
    val meta = spectrum.metaData.find(_.name == field).get
    if (meta == null) {
      "0"
    }
    else {
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
    p.println(s"ID: ${spectrum.id}")
    p.println(s"MW: ${buildCompoundMetaData(spectrum, "total exact mass")}")
    p.println(s"Formula: ${buildCompoundMetaData(spectrum, "molecule formula")}")
    p.println(s"Comments: ${buildComments(spectrum)}")
    p.println(s"PrecursorMZ: ${buildMetaDateField(spectrum, "precursor m/z")}")

    val spectra = buildSpectraString(spectrum)
    p.println(s"Num Peaks: ${spectra.length}")

    spectra.foreach { value =>
      p.println(s"${value}")
    }

    p.println()
    p.flush()
  }
}
