package edu.ucdavis.fiehnlab.mona.backend.core.domain.util.io.json

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.funsuite.AnyFunSuite
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
/**
  * Created by wohlgemuth on 2/25/16.
  */
class JSONDomainReader$Test extends AnyFunSuite {

  test("testCreate") {
    val reader: DomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

    assert(reader.isInstanceOf[JSONDomainReader[Spectrum]])
  }
}
