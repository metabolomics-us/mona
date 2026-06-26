package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CompoundTestApplication
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

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

    "recognize a syntactically valid InChIKey" in {
      assert(classyfireProcessor.isValidInchiKey("RYYVLZVUVIJVGH-UHFFFAOYSA-N"))
      assert(!classyfireProcessor.isValidInchiKey(null))
      assert(!classyfireProcessor.isValidInchiKey(""))
      assert(!classyfireProcessor.isValidInchiKey("   "))
      assert(!classyfireProcessor.isValidInchiKey("not-an-inchikey"))
    }

    "skip a compound that already has classification data without calling ClassyFire" in {
      val compound: Compound = new Compound()
      compound.setMetaData(List[MetaData]().asJava)
      compound.setClassification(List[MetaData](new MetaData(null, "kingdom", "Organic compounds", false, "classification", true, null)).asJava)

      val result: Compound = classyfireProcessor.classify(compound, "test-id")

      assert(result.getClassification.size() == 1)
      assert(result.getClassification.asScala.exists(_.getName == "kingdom"))
    }

    "skip a compound with no valid InChIKey without calling ClassyFire" in {
      val compound: Compound = new Compound()
      compound.setMetaData(List[MetaData]().asJava)
      compound.setClassification(List[MetaData]().asJava)
      compound.setInchiKey("")

      val result: Compound = classyfireProcessor.classify(compound, "test-id")

      assert(result.getClassification.isEmpty)
    }

    "report a spectrum as pending when a compound still carries a scheduled query id" in {
      val pendingCompound: Compound = new Compound()
      pendingCompound.setClassification(List[MetaData](new MetaData(null, CommonMetaData.CLASSYFIRE_QUERY_ID, "123", true, "none", false, null)).asJava)
      val pendingSpectrum: Spectrum = new Spectrum()
      pendingSpectrum.setCompound(List[Compound](pendingCompound).asJava)

      assert(classyfireProcessor.hasPendingClassification(pendingSpectrum))

      val doneCompound: Compound = new Compound()
      doneCompound.setClassification(List[MetaData](new MetaData(null, "kingdom", "Organic compounds", false, "classification", true, null)).asJava)
      val doneSpectrum: Spectrum = new Spectrum()
      doneSpectrum.setCompound(List[Compound](doneCompound).asJava)

      assert(!classyfireProcessor.hasPendingClassification(doneSpectrum))
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
