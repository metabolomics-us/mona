package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.io.json

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.wordspec.AnyWordSpec
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import java.io.{InputStreamReader, StringReader, StringWriter}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
  * Created by wohlgemuth on 5/27/16.
  */
@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class JSONDomainWriterTest extends AnyWordSpec with LazyLogging {

  "we should be able to create an instance of the writer" when {
    val reader = JSONDomainReader.create[SpectrumResult]
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: SpectrumResult = reader.read(input)

    val writer = new JSONDomainWriter

    "a writer" should {
      val out = new StringWriter()

      "write a spectrum out" in {
        writer.write(spectrum, out)
      }

      "and we must be able to read it again" in {
        val spectrumReRead: SpectrumResult = reader.read(new StringReader(out.toString))

        //stupid arrays break simple equality check....
        assert(spectrumReRead.getMonaId == spectrum.getMonaId)
        assert(spectrumReRead.getSpectrum.getMetaData.size() == spectrum.getSpectrum.getMetaData.size())
      }
    }
  }
}
