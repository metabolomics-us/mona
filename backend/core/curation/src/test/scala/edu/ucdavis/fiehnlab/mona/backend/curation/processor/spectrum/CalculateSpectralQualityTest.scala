package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec

/**
  * Created by sajjan on 4/21/16.
  */
class CalculateSpectralQualityTest extends AnyWordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new CalculateSpectralQuality

    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "given a spectra" must {

    }
  }
}
