package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.scalatest.wordspec.AnyWordSpec
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 4/05/16.
  */
class NormalizeIonizationModeValueTest extends AnyWordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {

    val processor = new NormalizeIonizationModeValue

    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    val baseSpectrum: Spectrum = reader.read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))

    // Build a spectrum whose only ionization mode field carries the given value
    def spectrumWithIonizationMode(value: String): Spectrum = {
      val spectrum = new Spectrum(baseSpectrum)
      val withoutIonMode = spectrum.getMetaData.asScala.filter(_.getName.toLowerCase != CommonMetaData.IONIZATION_MODE.toLowerCase)
      spectrum.setMetaData((withoutIonMode :+ new MetaData("", CommonMetaData.IONIZATION_MODE, value, false, "", false, "")).asJava)
      spectrum
    }

    def ionizationModeValue(spectrum: Spectrum): String =
      spectrum.getMetaData.asScala.find(_.getName == CommonMetaData.IONIZATION_MODE).map(_.getValue.toString).getOrElse("")

    "given a spectra" must {
      "verify the presence of an ionization mode metadata" in {
        exampleRecords.foreach { spectrum: Spectrum =>
          val processedSpectrum = processor.process(spectrum)

          assert(processedSpectrum.getMetaData.asScala.exists(_.getName == CommonMetaData.IONIZATION_MODE))
          assert(processedSpectrum.getScore.getImpacts.asScala.exists(_.getReason.toLowerCase.contains("ionization mode/type")))
        }
      }

      "normalize a wildcard positive value such as 'ESI Positive'" in {
        val processedSpectrum = processor.process(spectrumWithIonizationMode("ESI Positive"))
        assert(ionizationModeValue(processedSpectrum) == "positive")
      }

      "normalize a wildcard negative value such as 'ESI Negative'" in {
        val processedSpectrum = processor.process(spectrumWithIonizationMode("ESI Negative"))
        assert(ionizationModeValue(processedSpectrum) == "negative")
      }

      "still normalize an exact term value such as 'POS'" in {
        val processedSpectrum = processor.process(spectrumWithIonizationMode("POS"))
        assert(ionizationModeValue(processedSpectrum) == "positive")
      }

      "leave an unidentifiable value unchanged" in {
        val processedSpectrum = processor.process(spectrumWithIonizationMode("unknown"))
        assert(ionizationModeValue(processedSpectrum) == "unknown")
      }
    }
  }
}
