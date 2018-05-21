package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import java.io.BufferedWriter

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.sdf.SDFWriter
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 9/13/16.
  */
@Service
class SDFDownloader extends SpectrumDownloader {

  val mspWriter: SDFWriter = new SDFWriter


  /**
    * File format prefix
    *
    * @return
    */
  override def getContentPrefix: String = ""

  /**
    * File format suffix
    *
    * @return
    */
  override def getContentSuffix: String = ""

  /**
    * File format separator
    *
    * @return
    */
  override def getRecordSeparator: String = "\n"

  /**
    *
    */
  override def writeSpectrum(spectrum: Spectrum, bufferedWriter: BufferedWriter): Unit = {
    mspWriter.write(spectrum, bufferedWriter)
  }
}
