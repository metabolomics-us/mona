package edu.ucdavis.fiehnlab.mona.backend.curation.reader


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.scalatest.WordSpec
import org.springframework.batch.item.ExecutionContext

/**
  * Created by wohlgemuth on 3/18/16.
  */
class JSONFileSpectraReaderTest extends WordSpec with LazyLogging{

  "JSONFileSpectraReaderTest" should {

    val reader = new JSONFileSpectraReader

    val ctx = new ExecutionContext()
    "open" in {

      reader.stream = getClass.getResourceAsStream("/monaRecords.json")
      reader.open(ctx)
    }

    "read" in {

      var spectra:Spectrum = reader.read()

      var counter = 0
      while( spectra != null){
        counter = counter + 1
        assert(spectra.id != null)
        assert(spectra.spectrum != null)
        assert(spectra.biologicalCompound != null)
        spectra = reader.read()

      }
      assert(counter == 58)
    }


    "close" in {
      reader.close()
    }

  }
}
