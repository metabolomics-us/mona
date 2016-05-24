package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by sajjan on 4/05/16.
  */
class NormalizeMetaDataNamesTest extends WordSpec {

  val reader = JSONDomainReader.create[Spectrum]

  "this processor" when {

    val processor = new NormalizeMetaDataNames

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrumGiven: Spectrum = reader.read(input)


    "given a spectra" must {

      val processedSpectrum = processor.process(spectrumGiven)

    }
  }
}
