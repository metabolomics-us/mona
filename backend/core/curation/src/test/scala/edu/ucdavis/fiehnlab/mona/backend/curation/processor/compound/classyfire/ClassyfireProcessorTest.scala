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
import org.springframework.http.HttpHeaders
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import java.time.{ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter
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

    // Drive the shared adaptive spacing back down to the floor so each pacing test starts from a known state.
    // recordSuccess decays one step every spacingRecoveryThreshold calls, so a large run clamps it to the floor
    def resetSpacingToFloor(): Unit = (1 to 400).foreach(_ => classyfireProcessor.recordSuccess())

    "widen the shared request spacing on a 429, capped at the maximum" in {
      resetSpacingToFloor()
      assert(classyfireProcessor.currentSpacing == 2500)

      classyfireProcessor.widenSpacing()
      assert(classyfireProcessor.currentSpacing == 3750) // round(2500 * 1.5)

      classyfireProcessor.widenSpacing()
      assert(classyfireProcessor.currentSpacing == 5625) // round(3750 * 1.5)

      // repeated 429s never push the spacing past the configured cap
      (1 to 20).foreach(_ => classyfireProcessor.widenSpacing())
      assert(classyfireProcessor.currentSpacing == 15000)
    }

    "decay the spacing after a run of successes, never below the floor" in {
      resetSpacingToFloor()
      classyfireProcessor.widenSpacing()
      classyfireProcessor.widenSpacing()
      val widened: Long = classyfireProcessor.currentSpacing // 5625, success streak reset to 0

      // fewer than the recovery threshold of successes leaves the spacing unchanged
      (1 to 4).foreach(_ => classyfireProcessor.recordSuccess())
      assert(classyfireProcessor.currentSpacing == widened)

      // the threshold success triggers exactly one decay step
      classyfireProcessor.recordSuccess()
      assert(classyfireProcessor.currentSpacing == widened - 250)

      // a long run of successes decays back to, but never below, the floor
      (1 to 1000).foreach(_ => classyfireProcessor.recordSuccess())
      assert(classyfireProcessor.currentSpacing == 2500)
    }

    "honor a numeric Retry-After header as seconds" in {
      val headers: HttpHeaders = new HttpHeaders()
      headers.set(HttpHeaders.RETRY_AFTER, "2")

      assert(classyfireProcessor.parseRetryAfter(headers).contains(2000L))
    }

    "honor an HTTP-date Retry-After header" in {
      val headers: HttpHeaders = new HttpHeaders()
      val future: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(30)
      headers.set(HttpHeaders.RETRY_AFTER, future.format(DateTimeFormatter.RFC_1123_DATE_TIME))

      val delay: Option[Long] = classyfireProcessor.parseRetryAfter(headers)
      assert(delay.isDefined)
      assert(delay.get > 0 && delay.get <= 30000)
    }

    "fall back to no Retry-After when the header is absent or unparseable" in {
      assert(classyfireProcessor.parseRetryAfter(new HttpHeaders()).isEmpty)
      assert(classyfireProcessor.parseRetryAfter(null).isEmpty)

      val garbage: HttpHeaders = new HttpHeaders()
      garbage.set(HttpHeaders.RETRY_AFTER, "not-a-delay")
      assert(classyfireProcessor.parseRetryAfter(garbage).isEmpty)
    }
  }
}
