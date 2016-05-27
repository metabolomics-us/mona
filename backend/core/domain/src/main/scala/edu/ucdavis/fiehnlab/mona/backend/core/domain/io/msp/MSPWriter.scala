package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp

import java.io.Writer

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainWriter

/**
  * Created by wohlgemuth on 5/27/16.
  */
class MSPWriter extends DomainWriter{

  /**
    * write the output as a valid NISTMS msp file
    * @param spectrum
    * @return
    */
  def write(spectrum:Spectrum, writer: Writer) : Unit = ???
}
