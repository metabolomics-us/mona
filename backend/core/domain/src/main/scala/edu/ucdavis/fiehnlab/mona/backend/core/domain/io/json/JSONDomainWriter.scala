package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainWriter

import java.io.Writer

/**
  * Created by wohlgemuth on 5/27/16.
  */
class JSONDomainWriter extends DomainWriter {

  override val CRLF: Boolean = false

  val mapper: ObjectMapper = MonaMapper.create

  /**
    * writes the spectrum as a JSON representation
    *
    * @param spectrum
    * @return
    */
  override def write(spectrum: Spectrum, writer: Writer): Unit = mapper.writeValue(writer, spectrum)
}
