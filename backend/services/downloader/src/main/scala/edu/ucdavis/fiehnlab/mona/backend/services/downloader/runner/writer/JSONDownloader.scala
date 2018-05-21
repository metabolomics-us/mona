package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import java.io.BufferedWriter

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 9/13/16.
  */
@Service
class JSONDownloader extends SpectrumDownloader {

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
  override def writeSpectrum(spectrum: Spectrum, bufferedWriter: BufferedWriter): Unit = {
    bufferedWriter.write(objectMapper.writeValueAsString(spectrum))
  }
}
