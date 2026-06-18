package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire

import java.io.InputStreamReader
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CompoundTestApplication
import org.junit.runner.RunWith
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner

import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 5/5/16.
  */
@SpringBootTest(classes = Array(classOf[CompoundTestApplication], classOf[RestClientConfig]))
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class ClassyfireProcessorTest extends AnyWordSpec with LazyLogging {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  @Autowired
  val classyfireProcessor: ClassyfireProcessor = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ClassyfireProcessorTest" should {
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))
    val spectrumGiven: Spectrum = exampleRecords.head

    "process" in {
      assert(classyfireProcessor != null)

      if (classyfireProcessor.isReachable) {
        logger.info(s"Test faulty in CI, needs rework")
//        val output = classyfireProcessor.process(spectrumGiven)
//
//        output.getCompound.asScala.foreach { compound =>
//          assert(compound.getClassification.size() > 0)
//        }
      } else {
        logger.error("ClassyFire is offline - skipping test")
      }

    }

    "flag a compound as pending when ClassyFire is unavailable" in {
      val compound: Compound = new Compound()
      compound.setMetaData(List[MetaData]().asJava)

      classyfireProcessor.markClassyfireUnavailable(compound, "test-id")

      val marker: Option[MetaData] = compound.getMetaData.asScala.find(_.getName == CommonMetaData.CLASSYFIRE_STATUS)
      assert(marker.isDefined)
      assert(marker.get.getValue == CommonMetaData.CLASSYFIRE_STATUS_UNAVAILABLE)
      // computed so RemoveComputedData strips it on the next run, hidden so it stays out of the metadata table
      assert(marker.get.getComputed)
      assert(marker.get.getHidden)
    }

    "not duplicate the pending marker when flagged twice" in {
      val compound: Compound = new Compound()
      compound.setMetaData(List[MetaData]().asJava)

      classyfireProcessor.markClassyfireUnavailable(compound, "test-id")
      classyfireProcessor.markClassyfireUnavailable(compound, "test-id")

      assert(compound.getMetaData.asScala.count(_.getName == CommonMetaData.CLASSYFIRE_STATUS) == 1)
    }
  }
}
