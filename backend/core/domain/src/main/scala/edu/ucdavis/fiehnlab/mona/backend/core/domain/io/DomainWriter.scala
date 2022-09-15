package edu.ucdavis.fiehnlab.mona.backend.core.domain.io

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.CompoundDAO

import java.io.{OutputStream, OutputStreamWriter, PrintWriter, Writer}
import scala.jdk.CollectionConverters._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult

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
  def getAdditionalMetaData(spectrum: SpectrumResult, excludedMetaData: Array[String] = Array()): Array[(String, Any)] = {
    val compound: CompoundDAO = spectrum.getSpectrum.getCompound.asScala.find(_.getKind == "biological").getOrElse(spectrum.getSpectrum.getCompound.asScala.head)

    val metadata = (compound.getMetaData.asScala ++ spectrum.getSpectrum.getMetaData.asScala)
      .filter(x => !excludedMetaData.contains(x.getName.toLowerCase))
      .filter(x => x.getValue != null)
      .map(x => {
        if (x.getComputed) {
          ("computed " + x.getName, x.getValue.replaceAll("\"", ""))
        } else {
          (x.getName, x.getValue.replaceAll("\"", ""))
        }
      })

    if (spectrum.getSpectrum.getSplash != null) {
      metadata.append(("SPLASH", spectrum.getSpectrum.getSplash.getSplash))
    }

    if (spectrum.getSpectrum.getSubmitter != null) {
      metadata.append(("submitter", spectrum.getSpectrum.getSubmitter.toString))
    }

    if (spectrum.getSpectrum.getScore != null && spectrum.getSpectrum.getScore.getScore > 0) {
      metadata.append(("MoNA Rating", spectrum.getSpectrum.getScore.getScore.toString))
    }

    metadata.toArray
  }

  /**
    * writes the provided spectrum to the defined writer
    *
    * @param spectrum
    * @return
    */
  def write(spectrum: SpectrumResult, writer: Writer): Unit

  def write(spectrum: SpectrumResult, outputStream: OutputStream): Unit = write(spectrum, new OutputStreamWriter(outputStream))
}
