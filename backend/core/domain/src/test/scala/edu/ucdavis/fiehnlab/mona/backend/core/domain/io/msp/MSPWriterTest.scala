package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp

import java.io.{InputStreamReader, StringWriter}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 5/27/2016.
  */
class MSPWriterTest extends WordSpec {

  "we should be able to create an instance of the writer" when {
    val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]
    val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: Spectrum = reader.read(input)

    val writer: MSPWriter = new MSPWriter

    "a writer" should {

      val out = new StringWriter()

      "write a spectrum out" in {
        writer.write(spectrum, out)
      }

      "result is" in {
        println(out.toString)
      }

      "result must contain" must {
        "inchi key" in {
          assert(out.toString.contains("InChIKey: QASFUMOKHFSJGL-LAFRSMQTSA-N"))
        }

        "inchi code" in {
          assert(out.toString.contains("InChI Code=InChI=1S/C27H41NO2/c1-15-11-24-25(28-14-15)17(3)27(30-24)10-8-20-21-6-5-18-12-19(29)7-9-26(18,4)23(21)13-22(20)16(27)2/h5,15,17,19-21,23-25,28-29H,6-14H2,1-4H3"))
        }

        "has synonyms" in {
          assert(out.toString.contains("Synon: (3beta,22S,23R)-17,23-Epoxyveratraman-3-ol"))
        }

        "have precursor m/z" in {
          assert(out.toString.contains("PrecursorMZ"))
          assert(out.toString.contains("PrecursorMZ: 412.3"))
        }
      }
    }
  }
}
