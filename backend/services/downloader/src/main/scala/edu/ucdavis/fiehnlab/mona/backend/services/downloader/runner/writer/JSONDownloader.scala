package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import java.nio.file.Path
import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.QueryExport

/**
  * Created by sajjan on 9/13/16.
  */
class JSONDownloader(export: QueryExport, downloadDir: Path, compress: Boolean = true) extends SpectrumDownloader(export, downloadDir, compress) {

  val objectMapper: ObjectMapper = MonaMapper.create


  /**
    * File format prefix
    *
    * @return
    */
  override def getContentPrefix: String = "[\n"

  /**
    * File format suffix
    *
    * @return
    */
  override def getContentSuffix: String = "\n]"

  /**
    * File format separator
    *
    * @return
    */
  override def getRecordSeparator: String = ",\n"

  /**
    *
    */
  override def writeSpectrum(spectrum: Spectrum): Unit = exportWriter.write(objectMapper.writeValueAsString(spectrum))
}
