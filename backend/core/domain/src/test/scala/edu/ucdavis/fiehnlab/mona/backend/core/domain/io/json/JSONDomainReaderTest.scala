package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json

import java.io.FileReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types._
import org.scalatest.FunSuite

/**
  * Created by wohlgemuth on 2/25/16.
  */
class JSONDomainReaderTest extends FunSuite {

  test("testReadSingleSpectraInFile") {

    val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

    val input = new FileReader("src/test/resources/monaRecord.json")

    val spectrum: Spectrum = reader.read(input)

    assert(spectrum.splash.get.splash.get == "splash10-0z50000000-9c8c58860a0fadd33800")
    assert(spectrum.chemicalCompound.get.inchiKey.get == "QASFUMOKHFSJGL-LAFRSMQTSA-N")

  }

  test("testReadArrayContentInFile") {
    val reader: JSONDomainReader[Array[Spectrum]] = JSONDomainReader.create[Array[Spectrum]]

    val input = new FileReader("src/test/resources/monaRecords.json")

    val result: Array[Spectrum] = reader.read(input)

    assert(result.length == 58)

  }

}
