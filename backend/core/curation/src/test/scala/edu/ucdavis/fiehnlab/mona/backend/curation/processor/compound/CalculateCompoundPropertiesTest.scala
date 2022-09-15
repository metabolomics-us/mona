package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import com.typesafe.scalalogging.LazyLogging

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.junit.runner.RunWith
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner

import scala.jdk.CollectionConverters._
import scala.collection.mutable.{ArrayBuffer, Buffer}

/**
  * Created by sajjan on 9/26/16.
  */
@SpringBootTest(classes = Array(classOf[CompoundTestApplication], classOf[RestClientConfig]))
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class CalculateCompoundPropertiesTest extends AnyWordSpec with LazyLogging{

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

      result.getCompound.asScala.foreach { compound =>
        assert(compound.getMolFile != null)
        assert(compound.getMetaData.asScala.exists(_.getComputed))
      }

      assert(result.getScore != null)
      assert(result.getScore.getImpacts.asScala.nonEmpty)
      assert(result.getScore.getImpacts.asScala.map(_.getValue.asInstanceOf[Double]).sum == 4.0)
    }

    "handle problematic record PT201480" in {
      val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/PT201840.json"))
      val spectrum: Spectrum = reader.read(input)
      assert(spectrum.getId == "PT201840")

      assert(calculateCompoundProperties != null)
      val result: Spectrum = calculateCompoundProperties.process(spectrum)

      result.getCompound.asScala.foreach { compound =>
        assert(compound.getMolFile != null)
        assert(compound.getMetaData.asScala.exists(_.getComputed))
      }

      assert(result.getScore != null)
      assert(result.getScore.getImpacts.asScala.nonEmpty)
      assert(result.getScore.getImpacts.asScala.map(_.getValue.asInstanceOf[Double]).sum == 2.0)
    }

    "ensure that identifiers aren't duplicated when computed version matches the provided" in {
      val compound: CompoundDAO = new CompoundDAO(null, Buffer[TagDAO]().asJava, "", Buffer[Names]().asJava, "", false, "UYHMVWFYYZIVOP-RUAQSNJLSA-N", Buffer[MetaDataDAO]().asJava, null)
        //Compound("", "UYHMVWFYYZIVOP-RUAQSNJLSA-N", Array.empty[MetaData], "", Array.empty[Names], Array.empty[Tags], computed = false, null)
      val spectrum: Spectrum = new Spectrum(ArrayBuffer[CompoundDAO](compound).asJava, "test", Buffer[MetaDataDAO]().asJava, null, null, null, null, null, null, null, null, null, null)
        //Spectrum(Array(compound), "test", null, null, null, Array.empty[MetaData], null, null, null, null, null, null, null, null)

      val result: Spectrum = calculateCompoundProperties.process(spectrum)

      assert(result.getCompound.asScala.head.getMetaData.asScala.count(_.getName == CommonMetaData.INCHI_KEY) == 1)
    }
  }
}
