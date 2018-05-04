package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import java.io.BufferedWriter

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.png.PNGWriter
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 5/3/18.
  */
@Service
class StaticFileDownloader extends AbstractDownloader {

  val pngWriter: PNGWriter = new PNGWriter


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
  override def getFileSeparator: String = ""

  /**
    *
    */
  override def writeSpectrum(spectrum: Spectrum, bufferedWriter: BufferedWriter): Unit = {
    pngWriter.write(spectrum, bufferedWriter)
  }
}
