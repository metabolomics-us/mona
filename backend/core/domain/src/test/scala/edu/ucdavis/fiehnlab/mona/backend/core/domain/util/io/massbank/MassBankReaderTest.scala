package edu.ucdavis.fiehnlab.mona.backend.core.domain.util.io.massbank

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.massbank.MassBankReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.upload.RawParsedSpectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainReadEventHandler
import org.scalatest.wordspec.AnyWordSpec

import java.io.InputStreamReader

/**
  * Characterization tests for MassBankReader, run against real MassBank records copied from
  * angular-massbank-parser's own test_data fixtures (massbank-parser-lib.service.spec.ts),
  * covering records from several different contributing labs
  */
class MassBankReaderTest extends AnyWordSpec {

  private def parseResource(name: String): Option[RawParsedSpectrum] = {
    var result: Option[RawParsedSpectrum] = None
    val input = new InputStreamReader(getClass.getResourceAsStream(s"/upload/massbank/$name"))
    new MassBankReader().read(input, new DomainReadEventHandler[RawParsedSpectrum] {
      override def readEvent(event: RawParsedSpectrum): Unit = result = Some(event)
    })
    result
  }

  "MassBankReader" should {
    "parse a record from Boise State University" in {
      val res = parseResource("BSU00001.txt").get
      assert(res.names.contains("Veratramine"))
      assert(res.names.contains("(3beta,23R)-14,15,16,17-Tetradehydroveratraman-3,23-diol"))
      assert(res.meta.nonEmpty)
      assert(res.spectrum.nonEmpty)
    }

    "parse a record from Chubu University" in {
      val res = parseResource("UT000001.txt").get
      assert(res.names.contains("11,12-EET"))
      assert(res.meta.nonEmpty)
      assert(res.spectrum.nonEmpty)
    }

    "parse a record from Eawag, stripping the retention time unit" in {
      val res = parseResource("EA000401.txt").get
      assert(res.names.contains("Metamitron-desamino"))
      assert(res.meta.nonEmpty)
      assert(res.spectrum.nonEmpty)
    }

    "parse a record without a matching exception (regression fixture)" in {
      val res = parseResource("UF000108.txt").get
      assert(res.names.contains("Phenazine"))
      assert(res.meta.nonEmpty)
      assert(res.spectrum.nonEmpty)
    }
  }
}
