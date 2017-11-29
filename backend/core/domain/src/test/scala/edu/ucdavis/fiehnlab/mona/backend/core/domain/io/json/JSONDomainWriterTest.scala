package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json

import java.io.{InputStreamReader, StringReader, StringWriter}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.scalatest.WordSpec

/**
  * Created by wohlgemuth on 5/27/16.
  */
class JSONDomainWriterTest extends WordSpec with LazyLogging {

  "we should be able to create an instance of the writer" when {
    val reader = JSONDomainReader.create[Spectrum]
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: Spectrum = reader.read(input)

    val writer = new JSONDomainWriter

    "a writer" should {
      val out = new StringWriter()

      "write a spectrum out" in {
        writer.write(spectrum, out)
      }

      "and we must be able to read it again" in {
        val spectrumReRead: Spectrum = reader.read(new StringReader(out.toString))

        //stupid arrays break simple equality check....
        assert(spectrumReRead.id == spectrum.id)
        assert(spectrumReRead.metaData.size() == spectrum.metaData.size())
      }
    }
  }
}
