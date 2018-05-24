package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import java.nio.file.Path

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.png.PNGWriter
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.QueryExport

/**
  * Created by sajjan on 5/3/18.
  */
class PNGDownloader(export: QueryExport, downloadDir: Path, compress: Boolean = true) extends SpectrumDownloader(export, downloadDir, compress) {

  val pngWriter: PNGWriter = new PNGWriter


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
  override def getRecordSeparator: String = ""

  /**
    *
    */
  override def writeSpectrum(spectrum: Spectrum): Unit = pngWriter.write(spectrum, exportWriter)
}
