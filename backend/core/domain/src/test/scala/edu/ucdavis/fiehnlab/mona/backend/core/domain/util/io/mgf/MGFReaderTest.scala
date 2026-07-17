package edu.ucdavis.fiehnlab.mona.backend.core.domain.util.io.mgf

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.mgf.MGFReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.upload.RawParsedSpectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainReadEventHandler
import org.scalatest.wordspec.AnyWordSpec

import java.io.InputStreamReader

/**
  * Characterization tests for MGFReader, run against the same real fixture files
  * angular-mgf-parser's own mgf-parser-lib.service.spec.ts is tested with
  * (projects/mgf-parser-lib/test_data), copied into this module's test resources
  */
class MGFReaderTest extends AnyWordSpec {

  private def parseResource(name: String): List[RawParsedSpectrum] = {
    val buffer = scala.collection.mutable.ListBuffer[RawParsedSpectrum]()
    val input = new InputStreamReader(getClass.getResourceAsStream(s"/upload/mgf/$name"))
    new MGFReader().read(input, new DomainReadEventHandler[RawParsedSpectrum] {
      override def readEvent(event: RawParsedSpectrum): Unit = buffer += event
    })
    buffer.toList
  }

  "MGFReader" should {
    "parse mgf data from the Doerrstein MS/MS library" in {
      val results = parseResource("test.mgf")
      assert(results.length == 2)

      results.foreach { spectrum =>
        val names = spectrum.meta.map(_.name)
        assert(spectrum.meta.nonEmpty)
        assert(names.contains("INSTRUMENT"))
        assert(names.contains("MSLEVEL"))
        assert(names.contains("FILENAME"))
        assert(!names.contains("INCHI"))
        assert(!names.contains("SMILES"))
        assert(spectrum.inchi.contains("N/A"))
        assert(spectrum.spectrum.nonEmpty)
      }
    }

    "parse peaks even with no NAME field present" in {
      val results = parseResource("Hydroxy-fatty-acid-20CE.mgf")
      assert(results.length == 1)
      val res = results.head
      assert(res.names.isEmpty)
      assert(res.name.isEmpty)
      assert(res.spectrum.split(" ").length == 10)
    }
  }
}
