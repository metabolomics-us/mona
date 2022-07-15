package edu.ucdavis.fiehnlab.mona.backend.curation.reader


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.scalatest.wordspec.AnyWordSpec

/**
  * Created by wohlgemuth on 3/18/16.
  */
class JSONFileSpectraReaderTest extends AnyWordSpec with LazyLogging {

  "JSONFileSpectraReaderTest" should {

    val reader = new JSONFileSpectraReader

    "read" in {
      reader.stream = getClass.getResourceAsStream("/monaRecords.json")

      var spectra: Spectrum = reader.read()

      var counter = 0
      while (spectra != null) {
        counter = counter + 1
        assert(spectra.id != null)
        assert(spectra.spectrum != null)
        assert(spectra.compound != null)
        spectra = reader.read()

      }
      assert(counter == 59)
    }

  }
}
