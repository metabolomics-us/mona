package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.io.sdf

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import java.io.{InputStreamReader, StringWriter}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
/**
  * Created by sajjan on 2/21/18.
  */
@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SDFWriterTest extends AnyWordSpec {

  "a writer" should {
    "export monaRecord.json" must {
      val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
      val spectrum: SpectrumResult = JSONDomainReader.create[SpectrumResult].read(input)

      val writer: SDFWriter = new SDFWriter
      val out: StringWriter = new StringWriter()
      writer.write(spectrum, out)

      "Name" in {
        assert(out.toString.contains(">  <NAME>\r\nCY8"))
      }

      "synonyms" in {
        assert(out.toString.contains(">  <SYNONYMS>\r\nCyclopamine\r\n[3H]-Cyclopamine\r\n11-deoxojervine\r\n(3beta,22S,23R)-17,23-Epoxyveratraman-3-ol"))
      }

      "precursor type" in {
        assert(out.toString.contains(">  <PRECURSOR TYPE>\r\n[M+H]+"))
      }

      "spectrum type/ms level" in {
        assert(out.toString.contains(">  <SPECTRUM TYPE>\r\nMS2"))
      }

      "precursor m/z" in {
        assert(out.toString.contains(">  <PRECURSOR M/Z>\r\n412.3"))
      }

      "instrument type" in {
        assert(out.toString.contains(">  <INSTRUMENT TYPE>\r\nLC-ESI-QTOF"))
      }

      "instrument" in {
        assert(out.toString.contains(">  <INSTRUMENT>\r\nBruker maXis ESI-QTOF"))
      }

      "InChIKey" in {
        assert(out.toString.contains(">  <INCHIKEY>\r\nQASFUMOKHFSJGL-LAFRSMQTSA-N"))
      }

      "InChI" in {
        assert(out.toString.contains(">  <INCHI>\r\nInChI=1S/C27H41NO2/c1-15-11-24-25(28-14-15)17(3)27(30-24)10-8-20-21-6-5-18-12-19(29)7-9-26(18,4)23(21)13-22(20)16(27)2/h5,15,17,19-21,23-25,28-29H,6-14H2,1-4H3/t15-,17+,19-,20-,21-,23-,24+,25-,26-,27-/m0/s1"))
      }

      "molecular weight" in {
        assert(out.toString.contains(">  <MW>\r\n411"))
      }

      "DB#" in {
        assert(out.toString.contains(">  <ID>\r\n252"))
      }

      "exact mass" in {
        assert(out.toString.contains(">  <EXACT MASS>\r\n411.313"))
      }

      "correct number of peaks" in {
        assert(out.toString.contains(">  <NUM PEAKS>\r\n100"))
      }
    }

    "export first curatedRecords.json" must {
      val reader: JSONDomainReader[Array[SpectrumResult]] = JSONDomainReader.create[Array[SpectrumResult]]
      val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json"))
      val spectrum: SpectrumResult = reader.read(input).head

      val writer: SDFWriter = new SDFWriter
      val out: StringWriter = new StringWriter()
      writer.write(spectrum, out)

      println(out.toString)

      "Name" in {
        assert(out.toString.contains(">  <NAME>\r\nSulfaclozine"))
      }

      "synonyms" in {
        assert(out.toString.contains(">  <SYNONYMS>\r\n4-amino-N-(6-chloropyrazin-2-yl)benzenesulfonamide"))
      }

      "precursor type" in {
        assert(out.toString.contains(">  <PRECURSOR TYPE>\r\n[M+H]+"))
      }

      "spectrum type/ms level" in {
        assert(out.toString.contains(">  <SPECTRUM TYPE>\r\nMS2"))
      }

      "precursor m/z" in {
        assert(out.toString.contains(">  <PRECURSOR M/Z>\r\n285.0208"))
      }

      "instrument type" in {
        assert(out.toString.contains(">  <INSTRUMENT TYPE>\r\nLC-ESI-QTOF"))
      }

      "instrument" in {
        assert(out.toString.contains(">  <INSTRUMENT>\r\nBruker maXis Impact"))
      }

      "ionization mode" in {
        assert(out.toString.contains(">  <ION MODE>\r\nP"))
      }

      "collision energy" in {
        assert(out.toString.contains(">  <COLLISION ENERGY>\r\nRamp 21.1-31.6 eV"))
      }

      "InChIKey" in {
        assert(out.toString.contains(">  <INCHIKEY>\r\nQKLPUVXBJHRFQZ-UHFFFAOYSA-N"))
      }

      "InChI" in {
        assert(out.toString.contains(">  <INCHI>\r\nInChI=1S/C10H9ClN4O2S/c11-9-5-13-6-10(14-9)15-18(16,17)8-3-1-7(12)2-4-8/h1-6H,12H2,(H,14,15)"))
      }

      "molecular formula" in {
        assert(out.toString.contains(">  <FORMULA>\r\nC10H9ClN4O2S"))
      }

      "molecular weight" in {
        assert(out.toString.contains(">  <MW>\r\n284"))
      }

      "DB#" in {
        assert(out.toString.contains(">  <ID>\r\nAU100601"))
      }

      "exact mass" in {
        assert(out.toString.contains(">  <EXACT MASS>\r\n284.013474208"))
      }

      "correct number of peaks" in {
        assert(out.toString.contains(">  <NUM PEAKS>\r\n27"))
      }
    }

    "export curatedRecords.json" must {
      val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json"))
      val spectra: Array[SpectrumResult] = JSONDomainReader.create[Array[SpectrumResult]].read(input)

      val writer: SDFWriter = new SDFWriter
      val out: StringWriter = new StringWriter()
      spectra.foreach(x => writer.write(x, out))

      println(out.toString)

      "export the correct number of records" in {
        assert(out.toString.split("\\$\\$\\$\\$").length == 51)
      }
    }
  }
}
