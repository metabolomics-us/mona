package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
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

    "given a spectra" must {
      "verify the presence of an ionization mode metadata" in {
        exampleRecords.foreach { spectrum: Spectrum =>
          val processedSpectrum = processor.process(spectrum)

          assert(processedSpectrum.getMetaData.asScala.exists(_.getName == CommonMetaData.IONIZATION_MODE))
          assert(processedSpectrum.getScore.getImpacts.asScala.exists(_.getReason.toLowerCase.contains("ionization mode/type")))
        }
      }
    }
  }
}
