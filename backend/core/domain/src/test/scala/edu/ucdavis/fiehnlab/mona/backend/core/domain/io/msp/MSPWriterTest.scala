package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp

import java.io.{StringReader, StringWriter, InputStreamReader}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainWriter, JSONDomainReader}
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 5/27/2016.
  */
class MSPWriterTest extends WordSpec {

  "we should be able to create an instance of the writer" when {
    val reader = JSONDomainReader.create[Spectrum]
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: Spectrum = reader.read(input)

    val writer = new MSPWriter

    "a writer" should {

      val out = new StringWriter()

      "write a spectrum out" in {

        writer.write(spectrum,out)
      }

      "result is" in {
        println(out.toString)
      }

      "result must contain"  must {
        "inchi key" in {
          assert(out.toString.contains("InChIKey: QASFUMOKHFSJGL-LAFRSMQTSA-N"))

        }
      }
    }
  }
}
