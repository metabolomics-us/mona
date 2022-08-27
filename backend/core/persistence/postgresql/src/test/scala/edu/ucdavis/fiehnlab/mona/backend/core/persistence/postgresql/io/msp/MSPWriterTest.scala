package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.io.msp

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import java.io.{InputStreamReader, StringWriter}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.`type`.TypeReference

/**
  * Created by wohlg_000 on 5/27/2016.
  */
@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class MSPWriterTest extends AnyWordSpec {

  "we should be able to create an instance of the writer" when {
    val writer: MSPWriter = new MSPWriter

    "a writer" should {
      "export monaRecord.json" must {
        val reader: ObjectMapper = MonaMapper.create
        val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
        val temp: Spectrum = reader.readValue(input, new TypeReference[Spectrum] {})
        val spectrum: SpectrumResult = new SpectrumResult(temp.getId, temp)


        val out: StringWriter = new StringWriter()
        writer.write(spectrum, out)

        "result must contain" must {
          "Name" in {
            assert(out.toString.contains("Name: CY8"))
          }

          "synonyms" in {
            assert(out.toString.contains("Synon: Cyclopamine"))
            assert(out.toString.contains("Synon: 11-deoxojervine"))
            assert(out.toString.contains("Synon: (3beta,22S,23R)-17,23-Epoxyveratraman-3-ol"))
          }

          "in-source NIST tag" in {
            assert(out.toString.contains("Synon: $:00in-source"))
          }

          "DB#" in {
            assert(out.toString.contains("DB#: 252"))
          }

          "precursor type" in {
            assert(out.toString.contains("Precursor_type: [M+H]+"))
          }

          "spectrum type/ms level" in {
            assert(out.toString.contains("Spectrum_type: MS2"))
          }

          "precursor m/z" in {
            assert(out.toString.contains("PrecursorMZ: 412.3"))
          }

          "instrument type" in {
            assert(out.toString.contains("Instrument_type: LC-ESI-QTOF"))
          }

          "instrument" in {
            assert(out.toString.contains("Instrument: Bruker maXis ESI-QTOF"))
          }

          "InChIKey" in {
            assert(out.toString.contains("InChIKey: QASFUMOKHFSJGL-LAFRSMQTSA-N"))
          }

          "molecular weight" in {
            assert(out.toString.contains("MW: 411"))
          }

          "exact mass" in {
            assert(out.toString.contains("ExactMass: 411.313"))
          }

          "InChI" in {
            assert(out.toString.contains("InChI=1S/C27H41NO2/c1-15-11-24-25(28-14-15)17(3)27(30-24)10-8-20-21-6-5-18-12-19(29)7-9-26(18,4)23(21)13-22(20)16(27)2/h5,15,17,19-21,23-25,28-29H,6-14H2,1-4H3"))
          }

          "correct number of peaks" in {
            assert(out.toString.contains("Num Peaks: 100"))
          }
        }
      }

     /* "export first curatedRecords.json" must {
        val reader: JSONDomainReader[Array[SpectrumResult]] = JSONDomainReader.create[Array[SpectrumResult]]
        val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json"))
        val spectrum: SpectrumResult = reader.read(input).head

        val out: StringWriter = new StringWriter()
        writer.write(spectrum, out)

        "result must contain" must {
          "Name" in {
            assert(out.toString.contains("Name: Sulfaclozine"))
          }

          "synonyms" in {
            assert(out.toString.contains("Synon: 4-amino-N-(6-chloropyrazin-2-yl)benzenesulfonamide"))
          }

          "in-source NIST tag" in {
            assert(out.toString.contains("Synon: $:00in-source"))
          }

          "DB#" in {
            assert(out.toString.contains("DB#: AU100601"))
          }

          "precursor type" in {
            assert(out.toString.contains("Precursor_type: [M+H]+"))
          }

          "spectrum type/ms level" in {
            assert(out.toString.contains("Spectrum_type: MS2"))
          }

          "precursor m/z" in {
            assert(out.toString.contains("PrecursorMZ: 285.0208"))
          }

          "instrument type" in {
            assert(out.toString.contains("Instrument_type: LC-ESI-QTOF"))
          }

          "instrument" in {
            assert(out.toString.contains("Instrument: Bruker maXis Impact"))
          }

          "ionization mode" in {
            assert(out.toString.contains("Ion_mode: P"))
          }

          "collision energy" in {
            assert(out.toString.contains("Collision_energy: Ramp 21.1-31.6 eV"))
          }

          "InChIKey" in {
            assert(out.toString.contains("InChIKey: QKLPUVXBJHRFQZ-UHFFFAOYSA-N"))
          }

          "formula" in {
            assert(out.toString.contains("Formula: C10H9ClN4O2S"))
          }

          "molecular weight" in {
            assert(out.toString.contains("MW: 284"))
          }

          "exact mass" in {
            assert(out.toString.contains("ExactMass: 284.013474208"))
          }

          "InChI" in {
            assert(out.toString.contains("InChI=1S/C10H9ClN4O2S/c11-9-5-13-6-10(14-9)15-18(16,17)8-3-1-7(12)2-4-8/h1-6H,12H2,(H,14,15)"))
          }

          "correct number of peaks" in {
            assert(out.toString.contains("Num Peaks: 27"))
          }
        }
      }*/
    }
  }
}
