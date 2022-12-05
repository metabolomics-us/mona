package edu.ucdavis.fiehnlab.mona.backend.curation.processor

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec
import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 3/11/16.
  */
class RemoveComputedDataTest extends AnyWordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new RemoveComputedData

    val spectrumGiven: Spectrum = reader.read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))

    "given a spectra" must {
      val processedSpectrum = processor.process(spectrumGiven)

      "remove the computed metadata for the spectrum" in {
        assert(processedSpectrum.getMetaData.asScala.forall(!_.getComputed))
      }

      "remove the computed metadata for each compound" in {
        processedSpectrum.getCompound.asScala.foreach { compound =>
          assert(compound.getMetaData.asScala.forall(!_.getComputed))
        }
      }

      "remove the computed names for each compound" in {
        processedSpectrum.getCompound.asScala.foreach { compound =>
          assert(compound.getNames.asScala.forall(!_.getComputed))
        }
      }

      "remove the computed tags for the spectrum" in {
        assert(processedSpectrum.getTags.asScala.forall(!_.getRuleBased))
      }

      "remove the computed tags for each compound" in {
        processedSpectrum.getCompound.asScala.foreach { compound =>
          assert(compound.getTags.asScala.forall(!_.getRuleBased))
        }
      }
    }
  }
}
