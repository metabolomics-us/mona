package edu.ucdavis.fiehnlab.mona.backend.core.domain.io

import java.io.{OutputStream, OutputStreamWriter, PrintWriter, Writer}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum

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
    * writes the provided spectrum to the defined writer
    *
    * @param spectrum
    * @return
    */
  def write(spectrum: Spectrum, writer: Writer): Unit

  def write(spectrum: Spectrum, outputStream: OutputStream): Unit = write(spectrum, new OutputStreamWriter(outputStream))
}
