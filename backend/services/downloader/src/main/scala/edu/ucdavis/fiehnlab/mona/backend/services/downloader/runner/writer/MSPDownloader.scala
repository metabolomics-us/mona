package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import java.nio.file.Path

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp.MSPWriter
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.QueryExport
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult

/**
  * Created by sajjan on 9/13/16.
  */
class MSPDownloader(export: QueryExport, downloadDir: Path, compress: Boolean = true) extends SpectrumDownloader(export, downloadDir, compress) {

  val mspWriter: MSPWriter = new MSPWriter

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
  override def writeSpectrum(spectrum: SpectrumResult): Unit = mspWriter.write(spectrum, exportWriter)
}
