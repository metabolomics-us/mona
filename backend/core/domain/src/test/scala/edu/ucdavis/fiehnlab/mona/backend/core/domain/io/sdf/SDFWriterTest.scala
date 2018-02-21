package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.sdf

import java.io.{InputStreamReader, StringWriter}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 5/27/2016.
  */
class SDFWriterTest extends WordSpec {

  "we should be able to create an instance of the writer" when {
    val writer: SDFWriter = new SDFWriter

    "a writer" should {
      "export monaRecord.json" must {
        val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]
        val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
        val spectrum: Spectrum = reader.read(input)

        val out: StringWriter = new StringWriter()
        writer.write(spectrum, out)

        "Name" in {
          assert(out.toString.contains(">  <NAME>\nCY8"))
        }

        "synonyms" in {
          assert(out.toString.contains(">  <SYNONYMS>\nCyclopamine\n[3H]-Cyclopamine\n11-deoxojervine\n(3beta,22S,23R)-17,23-Epoxyveratraman-3-ol"))
        }

        "precursor type" in {
          assert(out.toString.contains(">  <PRECURSOR TYPE>\n[M+H]+"))
        }

        "spectrum type/ms level" in {
          assert(out.toString.contains(">  <SPECTRUM TYPE>\nMS2"))
        }

        "precursor m/z" in {
          assert(out.toString.contains(">  <PRECURSOR M/Z>\n412.3"))
        }

        "instrument type" in {
          assert(out.toString.contains(">  <INSTRUMENT TYPE>\nLC-ESI-QTOF"))
        }

        "instrument" in {
          assert(out.toString.contains(">  <INSTRUMENT>\nBruker maXis ESI-QTOF"))
        }

        "InChIKey" in {
          assert(out.toString.contains(">  <INCHIKEY>\nQASFUMOKHFSJGL-LAFRSMQTSA-N"))
        }

        "molecular weight" in {
          assert(out.toString.contains(">  <MW>\n411"))
        }

        "DB#" in {
          assert(out.toString.contains(">  <ID>\n252"))
        }

        "exact mass" in {
          assert(out.toString.contains(">  <EXACT MASS>\n411.313"))
        }

        "correct number of peaks" in {
          assert(out.toString.contains(">  <NUM PEAKS>\n100"))
        }
      }

      "export first curatedRecords.json" must {
        val reader: JSONDomainReader[Array[Spectrum]] = JSONDomainReader.create[Array[Spectrum]]
        val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json"))
        val spectrum: Spectrum = reader.read(input).head

        val out: StringWriter = new StringWriter()
        writer.write(spectrum, out)

        println(out.toString)

        "Name" in {
          assert(out.toString.contains(">  <NAME>\nSulfaclozine"))
        }

        "synonyms" in {
          assert(out.toString.contains(">  <SYNONYMS>\n4-amino-N-(6-chloropyrazin-2-yl)benzenesulfonamide"))
        }

        "precursor type" in {
          assert(out.toString.contains(">  <PRECURSOR TYPE>\n[M+H]+"))
        }

        "spectrum type/ms level" in {
          assert(out.toString.contains(">  <SPECTRUM TYPE>\nMS2"))
        }

        "precursor m/z" in {
          assert(out.toString.contains(">  <PRECURSOR M/Z>\n285.0208"))
        }

        "instrument type" in {
          assert(out.toString.contains(">  <INSTRUMENT TYPE>\nLC-ESI-QTOF"))
        }

        "instrument" in {
          assert(out.toString.contains(">  <INSTRUMENT>\nBruker maXis Impact"))
        }

        "ionization mode" in {
          assert(out.toString.contains(">  <ION MODE>\nP"))
        }

        "collision energy" in {
          assert(out.toString.contains(">  <COLLISION ENERGY>\nRamp 21.1-31.6 eV"))
        }

        "InChIKey" in {
          assert(out.toString.contains(">  <INCHIKEY>\nQKLPUVXBJHRFQZ-UHFFFAOYSA-N"))
        }

        "molecular formula" in {
          assert(out.toString.contains(">  <FORMULA>\nC10H9ClN4O2S"))
        }

        "molecular weight" in {
          assert(out.toString.contains(">  <MW>\n284"))
        }

        "DB#" in {
          assert(out.toString.contains(">  <ID>\nAU100601"))
        }

        "exact mass" in {
          assert(out.toString.contains(">  <EXACT MASS>\n284.013474208"))
        }

        "correct number of peaks" in {
          assert(out.toString.contains(">  <NUM PEAKS>\n27"))
        }
      }
    }
  }
}
