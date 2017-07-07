package edu.ucdavis.fiehnlab.mona.core.similarity.index

import java.io.InputStreamReader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by sajjan on 4/24/17.
  */
class PeakIndexTest extends WordSpec with Matchers with LazyLogging {

  "a peak index" should {

    val index: PeakIndex = new PeakIndex

    val records: Array[SimpleSpectrum] = JSONDomainReader
      .create[Array[Spectrum]]
      .read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))
      .map(s => new SimpleSpectrum(s.id, s.spectrum))

    val spectrum: SimpleSpectrum = records.head


    "be populated with 58 spectra" in {
      // Populate the database
      records.foreach(index.doIndex)

      index.size shouldEqual 166
    }

    "have a decent lookup time of the head record" in {
      for (i <- 0 to 100) {
        index.lookup(spectrum).get shouldEqual spectrum
      }
    }

    "should be able to retrieve results given a spectrum object" in {
      index.get(spectrum).size shouldBe 1
      index.get(spectrum) should contain (spectrum)
    }

    "should be able to retrieve results given a single m/z value" in {
      index.get(spectrum.ions.head.mz).size shouldBe 3
      index.get(spectrum) should contain (spectrum)
    }

    "should be able to retrieve results given an array of m/z values" in {
      index.get(spectrum.ions.map(_.mz)).size shouldBe 1
      index.get(spectrum) should contain (spectrum)
    }
  }
}
