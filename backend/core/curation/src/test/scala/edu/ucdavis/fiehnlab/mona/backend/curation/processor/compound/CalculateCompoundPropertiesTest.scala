package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.junit.runner.RunWith
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 9/26/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[CompoundTestApplication], classOf[RestClientConfig]))
class CalculateCompoundPropertiesTest extends AnyWordSpec {

  @Autowired
  val calculateCompoundProperties: CalculateCompoundProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "CalculateCompoundPropertiesTest" should {

    val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

    "process" in {
      val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
      val spectrum: Spectrum = reader.read(input)

      assert(calculateCompoundProperties != null)
      val result: Spectrum = calculateCompoundProperties.process(spectrum)

      result.compound.foreach { compound =>
        assert(compound.molFile != null)
        assert(compound.metaData.exists(_.computed))
      }

      assert(result.score != null)
      assert(result.score.impacts.nonEmpty)
      assert(result.score.impacts.map(_.value).sum == 4.0)
    }

    "handle problematic record PT201480" in {
      val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/PT201840.json"))
      val spectrum: Spectrum = reader.read(input)
      assert(spectrum.id == "PT201840")

      assert(calculateCompoundProperties != null)
      val result: Spectrum = calculateCompoundProperties.process(spectrum)

      result.compound.foreach { compound =>
        assert(compound.molFile != null)
        assert(compound.metaData.exists(_.computed))
      }

      assert(result.score != null)
      assert(result.score.impacts.nonEmpty)
      assert(result.score.impacts.map(_.value).sum == 2.0)
    }

    "ensure that identifiers aren't duplicated when computed version matches the provided" in {
      val compound: Compound = Compound("", "UYHMVWFYYZIVOP-RUAQSNJLSA-N", Array.empty[MetaData], "", Array.empty[Names], Array.empty[Tags], computed = false, null)
      val spectrum: Spectrum = Spectrum(Array(compound), "test", null, null, null, Array.empty[MetaData], null, null, null, null, null, null, null, null)

      val result: Spectrum = calculateCompoundProperties.process(spectrum)

      assert(result.compound.head.metaData.count(_.name == CommonMetaData.INCHI_KEY) == 1)
    }
  }
}
