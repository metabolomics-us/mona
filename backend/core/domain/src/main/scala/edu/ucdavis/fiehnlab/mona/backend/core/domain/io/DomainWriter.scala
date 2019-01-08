package edu.ucdavis.fiehnlab.mona.backend.core.domain.io

import java.io.{OutputStream, OutputStreamWriter, PrintWriter, Writer}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Spectrum}

/**
  * Created by wohlgemuth on 5/27/16.
  */
trait DomainWriter {

  /**
    * Handle CRLF
    */
  val CRLF: Boolean

  def getNewLine: String = {
    if (CRLF) {
      "\r\n"
    } else {
      "\n"
    }
  }

  def getPrintWriter(writer: Writer): PrintWriter = {
    if (CRLF) {
      new CRLFPrintWriter(writer)
    } else {
      new PrintWriter(writer)
    }
  }

  /**
    * Gets all miscellaneous metadata for comments fields
    */
  def getAdditionalMetaData(spectrum: Spectrum, excludedMetaData: Array[String] = Array()): Array[(String, Any)] = {
    val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

    val metadata = (compound.metaData ++ spectrum.metaData)
      .filter(x => !excludedMetaData.contains(x.name.toLowerCase))
      .filter(x => x.value != null)
      .map(x => {
        if (x.computed) {
          ("computed " + x.name, x.value.toString.replaceAll("\"", ""))
        } else {
          (x.name, x.value.toString.replaceAll("\"", ""))
        }
      })
      .toBuffer

    if (spectrum.splash != null) {
      metadata.append(("SPLASH", spectrum.splash.splash))
    }

    if (spectrum.submitter != null) {
      metadata.append(("submitter", spectrum.submitter.toString))
    }

    if (spectrum.score != null && spectrum.score.score > 0) {
      metadata.append(("MoNA Rating", spectrum.score.score.toString))
    }

    metadata.toArray
  }

  /**
    * writes the provided spectrum to the defined writer
    *
    * @param spectrum
    * @return
    */
  def write(spectrum: Spectrum, writer: Writer): Unit

  def write(spectrum: Spectrum, outputStream: OutputStream): Unit = write(spectrum, new OutputStreamWriter(outputStream))
}
