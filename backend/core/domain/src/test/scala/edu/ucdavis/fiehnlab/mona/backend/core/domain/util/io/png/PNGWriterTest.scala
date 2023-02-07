package edu.ucdavis.fiehnlab.mona.backend.core.domain.util.io.png

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.boot.test.context.SpringBootTest
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.png.PNGWriter

import java.io.{InputStreamReader, StringWriter}

/**
  * Created by sajjan on 4/30/2018.
  */
@SpringBootTest
class PNGWriterTest extends AnyWordSpec {

  "we should be able to create an instance of the writer" when {
    val writer: PNGWriter = new PNGWriter

    "a writer" should {
      "export monaRecord.json" must {
        val reader = JSONDomainReader.create[Spectrum]
        val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
        val spectrum: Spectrum = reader.read(input)

        val out: StringWriter = new StringWriter()
        writer.write(spectrum, out)

        "contain the spectrum id" in {
          assert(out.toString.startsWith("252"))
        }
      }

      "export first curatedRecords.json" must {
        val reader: JSONDomainReader[Array[Spectrum]] = JSONDomainReader.create[Array[Spectrum]]
        val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json"))
        val spectrum: Spectrum = reader.read(input).head

        val out: StringWriter = new StringWriter()
        writer.write(spectrum, out)

        "contain the spectrum id" in {
          assert(out.toString.startsWith("AU100601"))
        }
      }
    }
  }
}
