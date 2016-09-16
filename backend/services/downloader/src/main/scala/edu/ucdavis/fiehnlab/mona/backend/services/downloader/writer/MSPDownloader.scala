package edu.ucdavis.fiehnlab.mona.backend.services.downloader.writer

import java.io.BufferedWriter
import java.nio.file.Path

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp.MSPWriter
import org.springframework.stereotype.{Component, Service}

/**
  * Created by sajjan on 9/13/16.
  */
@Service
class MSPDownloader extends AbstractDownloader {

  val mspWriter: MSPWriter = new MSPWriter


  /**
    * File format prefix
    *
    * @return
    */
  override def getFilePrefix: String = ""

  /**
    * File format suffix
    *
    * @return
    */
  override def getFileSuffix: String = ""

  /**
    * File format separator
    *
    * @return
    */
  override def getFileSeparator: String = "\n"

  /**
    *
    */
  override def writeSpectrum(spectrum: Spectrum, bufferedWriter: BufferedWriter) = {
    mspWriter.write(spectrum, bufferedWriter)
  }
}
