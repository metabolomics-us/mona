package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainReader
import org.scalatest.FunSuite

/**
  * Created by wohlgemuth on 2/25/16.
  */
class JSONDomainReader$Test extends FunSuite {

  test("testCreate") {

    val reader:DomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

    assert(reader.isInstanceOf[JSONDomainReader[Spectrum]])
  }

}
