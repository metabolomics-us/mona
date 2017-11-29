package edu.ucdavis.fiehnlab.mona.backend.core.domain.util

import java.io.{File, FileInputStream, InputStream, InputStreamReader}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{LegacySpectrum, Spectrum}

/**
  * Created by wohlgemuth on 4/25/16.
  */
class LegacyConverter {

  /**
    * little very very memory inefficient converter
    *
    * @param inputStream
    * @return
    */
  def convert(inputStream: InputStream): Array[Spectrum] = {
    val reader = JSONDomainReader.create[Array[LegacySpectrum]]
    reader.read(new InputStreamReader(inputStream)).collect {
      case x: LegacySpectrum =>
        x.asSpectrum
    }
  }
}

/**
  * quick and dirty test file converter
  */
object LegacyConverter extends App {

  MonaMapper.create.writeValue(new File("src/test/resources/monaRecord.json"), new LegacyConverter().convert(new FileInputStream("src/test/resources/legacy/monaRecord.json")).head)
  MonaMapper.create.writeValue(new File("src/test/resources/monaRecords.json"), new LegacyConverter().convert(new FileInputStream("src/test/resources/legacy/monaRecords.json")))
}