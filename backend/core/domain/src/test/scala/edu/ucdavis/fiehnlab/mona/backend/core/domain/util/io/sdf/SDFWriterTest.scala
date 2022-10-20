package edu.ucdavis.fiehnlab.mona.backend.core.domain.util.io.sdf

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.boot.test.context.SpringBootTest
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.sdf.SDFWriter

import java.io.{InputStreamReader, StringWriter}
/**
  * Created by sajjan on 2/21/18.
  */
@SpringBootTest
class SDFWriterTest extends AnyWordSpec {

  "a writer" should {
    "export monaRecord.json" must {
      val reader: ObjectMapper = MonaMapper.create
      val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
      val spectrum: Spectrum = reader.readValue(input, new TypeReference[Spectrum] {})
      val spectrumResult: SpectrumResult = new SpectrumResult(spectrum.getId, spectrum)

      val writer: SDFWriter = new SDFWriter
      val out: StringWriter = new StringWriter()
      writer.write(spectrumResult, out)

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
      val reader: JSONDomainReader[Array[Spectrum]] = JSONDomainReader.create[Array[Spectrum]]
      val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json"))
      val spectrum: Spectrum = reader.read(input).head
      val spectrumResult: SpectrumResult = new SpectrumResult(spectrum.getId, spectrum)

      val writer: SDFWriter = new SDFWriter
      val out: StringWriter = new StringWriter()
      writer.write(spectrumResult, out)

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
      val spectra: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(input)

      val writer: SDFWriter = new SDFWriter
      val out: StringWriter = new StringWriter()
      spectra.foreach{x =>
        writer.write(new SpectrumResult(x.getId, x), out)
      }

      println(out.toString)

      "export the correct number of records" in {
        assert(out.toString.split("\\$\\$\\$\\$").length == 51)
      }
    }
  }
}
