package edu.ucdavis.fiehnlab.mona.backend.core.domain.io

import java.io.Writer

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum

/**
  * Created by wohlgemuth on 5/27/16.
  */
trait DomainWriter {

  /**
    * writes the provided spectrum to the defined writer
    * @param spectrum
    * @return
    */
  def write(spectrum:Spectrum, writer:Writer) : Unit

}
