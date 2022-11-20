package edu.ucdavis.fiehnlab.mona.backend.core.domain.util.io.json

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.boot.test.context.SpringBootTest
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainWriter

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

    val writer = new JSONDomainWriter

    "a writer" should {
      val out = new StringWriter()

      "write a spectrum out" in {
        writer.write(spectrum, out)
      }

      "and we must be able to read it again" in {
        logger.info(s"${out}")
        val reader2 = JSONDomainReader.create[Spectrum]
        val spectrumReRead: Spectrum = reader2.read(new StringReader(out.toString))

        //stupid arrays break simple equality check....
        assert(spectrumReRead.getId == spectrum.getId)
        assert(spectrumReRead.getMetaData.size() == spectrum.getMetaData.size())
      }
    }
  }
}
