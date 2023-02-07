package edu.ucdavis.fiehnlab.mona.core.similarity.index

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
  * Created by sajjan on 12/28/16.
  */
abstract class IndexTest extends AnyWordSpec with Matchers {

  def createIndex(): Index

  "An index" should {

    val index: Index = createIndex()
    val records: Array[SimpleSpectrum] = JSONDomainReader
      .create[Array[Spectrum]]
      .read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))
      .map(s => new SimpleSpectrum(s.getId, s.getSpectrum))

    "be populated with 59 spectra" in {
      // Populate the database
      records.foreach(index.index)

      index.size shouldEqual 59
    }

    "have a decent lookup time of the head record" in {
      val spectrum: SimpleSpectrum = records.head

      for (i <- 0 to 100) {
        index.lookup(spectrum).get.splash shouldEqual spectrum.splash
      }
    }

    "have a decent lookup time of the middle record" in {
      val spectrum: SimpleSpectrum = records(Math.ceil(records.length / 2).toInt)

      for (i <- 0 to 100) {
        index.lookup(spectrum).get.splash shouldEqual spectrum.splash
      }
    }

    "have a decent lookup time of the last record" in {
      val spectrum: SimpleSpectrum = records.last

      for (i <- 0 to 100) {
        index.lookup(spectrum).get.splash shouldEqual spectrum.splash
      }
    }
  }
}


class LinearIndexTest extends IndexTest {
  def createIndex(): Index = new LinearIndex
}

class HistogramIndexTest extends IndexTest {
  def createIndex(): Index = new HistogramIndex
}

class SimilarHistogramIndexTest extends IndexTest {
  def createIndex(): Index = new SimilarHistogramIndex
}
