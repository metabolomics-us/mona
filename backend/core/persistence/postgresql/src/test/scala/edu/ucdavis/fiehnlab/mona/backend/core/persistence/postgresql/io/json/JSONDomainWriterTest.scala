package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.io.json

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.boot.test.context.SpringBootTest
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum

import java.io.{InputStreamReader, StringReader, StringWriter}

/**
  * Created by wohlgemuth on 5/27/16.
  */
@SpringBootTest
class JSONDomainWriterTest extends AnyWordSpec with LazyLogging {

  "we should be able to create an instance of the writer" when {
    val reader = JSONDomainReader.create[Spectrum]
    val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: Spectrum = reader.read(input)
    val spectrumResult: SpectrumResult = new SpectrumResult(spectrum.getId, spectrum)

    val writer = new JSONDomainWriter

    "a writer" should {
      val out = new StringWriter()

      "write a spectrum out" in {
        writer.write(spectrumResult, out)
      }

      "and we must be able to read it again" in {
        logger.info(s"${out}")
        val reader2 = JSONDomainReader.create[SpectrumResult]
        val spectrumReRead: SpectrumResult = reader2.read(new StringReader(out.toString))

        //stupid arrays break simple equality check....
        assert(spectrumReRead.getMonaId == spectrum.getId)
        assert(spectrumReRead.getSpectrum.getMetaData.size() == spectrum.getMetaData.size())
      }
    }
  }
}
