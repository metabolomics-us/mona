package edu.ucdavis.fiehnlab.mona.backend.core.domain.io

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{CompoundDAO, Spectrum}

import java.io.{OutputStream, OutputStreamWriter, PrintWriter, Writer}
import scala.jdk.CollectionConverters._

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
    val compound: CompoundDAO = spectrum.getCompound.asScala.find(_.getKind == "biological").getOrElse(spectrum.getCompound.asScala.head)

    val metadata = (compound.getMetaData.asScala ++ spectrum.getMetaData.asScala)
      .filter(x => !excludedMetaData.contains(x.getName.toLowerCase))
      .filter(x => x.getValue != null)
      .map(x => {
        if (x.getComputed) {
          ("computed " + x.getName, x.getValue.replaceAll("\"", ""))
        } else {
          (x.getName, x.getValue.replaceAll("\"", ""))
        }
      })

    if (spectrum.getSplash != null) {
      metadata.append(("SPLASH", spectrum.getSplash.getSplash))
    }

    if (spectrum.getSubmitter != null) {
      metadata.append(("submitter", spectrum.getSubmitter.toString))
    }

    if (spectrum.getScore != null && spectrum.getScore.getScore > 0) {
      metadata.append(("MoNA Rating", spectrum.getScore.getScore.toString))
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
