package edu.ucdavis.fiehnlab.mona.backend.core.domain.util.io.json

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.funsuite.AnyFunSuite
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult
/**
  * Created by wohlgemuth on 2/25/16.
  */
class JSONDomainReader$Test extends AnyFunSuite {

  test("testCreate") {
    val reader: DomainReader[SpectrumResult] = JSONDomainReader.create[SpectrumResult]

    assert(reader.isInstanceOf[JSONDomainReader[SpectrumResult]])
  }
}
