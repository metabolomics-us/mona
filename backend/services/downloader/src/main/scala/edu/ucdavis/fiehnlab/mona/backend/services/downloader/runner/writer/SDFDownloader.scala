package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import java.nio.file.Path

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.sdf.SDFWriter
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.QueryExport

/**
  * Created by sajjan on 9/13/16.
  */
class SDFDownloader(export: QueryExport, downloadDir: Path, compress: Boolean = true) extends SpectrumDownloader(export, downloadDir, compress) {

  val sdfWriter: SDFWriter = new SDFWriter


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
  override def writeSpectrum(spectrum: Spectrum): Unit = sdfWriter.write(spectrum, exportWriter)
}
