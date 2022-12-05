package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import java.nio.file.{Files, Path}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.png.PNGWriter
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.QueryExport

/**
  * Created by sajjan on 5/3/18.
  */
class PNGDownloader(export: QueryExport, downloadDir: Path, compress: Boolean = true) extends SpectrumDownloader(export, downloadDir, compress) {

  val pngWriter: PNGWriter = new PNGWriter

  /**
    * Filename for this export
    *
    * @return
    */
  override def exportFilename: String = s"MoNA-export-$basename-spectrum-images.csv"

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
    * Create description file and prevent writing query file
    */
  override def writeAssociatedFiles(): Unit = {
    val descriptionFile: Path =
      if (compress)
        downloadDir.resolve(compressedExportFilename + ".description.txt")
      else
        downloadDir.resolve(exportFilename + ".description.txt")

    Files.write(descriptionFile, "Table of Base64-encoded spectrum images for all MoNA records".getBytes)
  }


  /**
    *
    */
  override def writeSpectrum(spectrum: Spectrum): Unit = pngWriter.write(spectrum, exportWriter)
}
