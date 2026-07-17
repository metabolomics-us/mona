package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{ClassificationCache, Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CompoundTestApplication
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestTemplate

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

    "treat an empty ClassyFire entities result as no classification" in {
      // ClassyFire answers a structure it has no entity record for with an empty {} body, deserialized to an
      // all null result. This must route to the queries endpoint rather than be stored as an empty classification
      val empty: ClassyfireResult = ClassyfireResult(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
      assert(classyfireProcessor.isEmptyClassification(empty))
      assert(classyfireProcessor.isEmptyClassification(null))

      val populated: ClassyfireResult = empty.copy(kingdom = Classification("Organic compounds", null, null, null))
      assert(!classyfireProcessor.isEmptyClassification(populated))
    }

    "honor a fresh negative cache entry but expire an old one, and never expire a positive entry" in {
      val mapper = MonaMapper.create
      val positiveJson: String = mapper.writeValueAsString(Array(new MetaData(null, "kingdom", "Organic compounds", false, "classification", true, null)))

      // a negative entry (empty payload) is usable while fresh
      assert(classyfireProcessor.isCacheEntryUsable(new ClassificationCache("ABCDEFGHIJKLMN", "[]", new java.util.Date())))
      // but expires once older than the TTL
      assert(!classyfireProcessor.isCacheEntryUsable(new ClassificationCache("ABCDEFGHIJKLMN", "[]", new java.util.Date(0))))
      // a positive entry is always usable, regardless of age
      assert(classyfireProcessor.isCacheEntryUsable(new ClassificationCache("ABCDEFGHIJKLMN", positiveJson, new java.util.Date(0))))

      assert(classyfireProcessor.isNegativeCache(new ClassificationCache("ABCDEFGHIJKLMN", "[]", new java.util.Date())))
      assert(!classyfireProcessor.isNegativeCache(new ClassificationCache("ABCDEFGHIJKLMN", positiveJson, new java.util.Date())))
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

    // Build a compound with a valid InChIKey and a computed structure, the minimum scheduleClassification needs
    def compoundWithStructure(inchiKey: String): Compound = {
      val compound: Compound = new Compound()
      compound.setInchiKey(inchiKey)
      compound.setMetaData(List[MetaData](new MetaData(null, CommonMetaData.INCHI_CODE, "InChI=1S/CH4/h1H4", false, "computed", true, null)).asJava)
      compound.setClassification(List[MetaData]().asJava)
      compound
    }

    def queryIdOf(compound: Compound): Option[String] =
      compound.getClassification.asScala.find(_.getName == CommonMetaData.CLASSYFIRE_QUERY_ID).map(_.getValue.toString)

    // A fresh processor with a stubbed RestOperations, so scheduling and polling never hit the network. Spacing
    // is zeroed so withRateLimit does not sleep between the stubbed calls
    def stubbedProcessor(stub: RestTemplate, stats: ClassyfireStats): ClassyfireProcessor = {
      val processor: ClassyfireProcessor = new ClassyfireProcessor()
      ReflectionTestUtils.setField(processor, "restOperations", stub)
      ReflectionTestUtils.setField(processor, "classyfireStats", stats)
      ReflectionTestUtils.setField(processor, "requestSpacingMs", 0L)
      ReflectionTestUtils.setField(processor, "currentSpacingMs", 0L)
      processor
    }

    "reuse an in flight ClassyFire query for a second compound sharing the InChIKey skeleton" in {
      val stub: StubClassyfireRestTemplate = new StubClassyfireRestTemplate()
      val stats: ClassyfireStats = new ClassyfireStats()
      val processor: ClassyfireProcessor = stubbedProcessor(stub, stats)

      // two different full InChIKeys sharing the same 14 character skeleton block
      val first: Compound = compoundWithStructure("HEFYAORHSGZUIX-UHFFFAOYSA-N")
      val second: Compound = compoundWithStructure("HEFYAORHSGZUIX-ABCDEFGHSA-N")

      processor.scheduleClassification(first, "spectrum-a")
      processor.scheduleClassification(second, "spectrum-b")

      // only the first compound POSTs, the second reuses the in flight query id
      assert(stub.scheduleCount == 1)
      assert(stats.newClassificationsScheduled.get() == 1)
      assert(stats.pendingQueriesReused.get() == 1)

      // both compounds end up carrying the same shared query id to poll
      assert(queryIdOf(first).isDefined)
      assert(queryIdOf(first) == queryIdOf(second))
    }

    "not reuse an in flight query across different InChIKey skeletons" in {
      val stub: StubClassyfireRestTemplate = new StubClassyfireRestTemplate()
      val stats: ClassyfireStats = new ClassyfireStats()
      val processor: ClassyfireProcessor = stubbedProcessor(stub, stats)

      processor.scheduleClassification(compoundWithStructure("HEFYAORHSGZUIX-UHFFFAOYSA-N"), "spectrum-a")
      processor.scheduleClassification(compoundWithStructure("RYYVLZVUVIJVGH-UHFFFAOYSA-N"), "spectrum-b")

      // distinct skeletons each get their own query, nothing is reused
      assert(stub.scheduleCount == 2)
      assert(stats.newClassificationsScheduled.get() == 2)
      assert(stats.pendingQueriesReused.get() == 0)
    }

    "stop reusing a query once it is Done so a later compound schedules a fresh one" in {
      val stub: StubClassyfireRestTemplate = new StubClassyfireRestTemplate()
      val stats: ClassyfireStats = new ClassyfireStats()
      val processor: ClassyfireProcessor = stubbedProcessor(stub, stats)

      val first: Compound = compoundWithStructure("HEFYAORHSGZUIX-UHFFFAOYSA-N")
      processor.scheduleClassification(first, "spectrum-a")
      assert(stub.scheduleCount == 1)

      // finishing the query must drop the in flight dedup entry (the stub polls back Done)
      processor.pollScheduledQuery(first, "spectrum-a", queryIdOf(first).get)

      // a later compound with the same skeleton no longer reuses, it schedules a fresh query
      val later: Compound = compoundWithStructure("HEFYAORHSGZUIX-UHFFFAOYSA-N")
      processor.scheduleClassification(later, "spectrum-c")

      assert(stub.scheduleCount == 2)
      assert(stats.pendingQueriesReused.get() == 0)
      assert(stats.newClassificationsScheduled.get() == 2)
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

/**
  * Stubs the two ClassyFire calls the dedup tests exercise, so scheduling and polling never touch the network.
  * Each POST to the queries endpoint returns a fresh incrementing query id and counts the call, and every poll
  * answers Done with a usable classification so pollScheduledQuery reaches its terminal path
  */
class StubClassyfireRestTemplate extends RestTemplate {
  var scheduleCount: Int = 0
  private var nextQueryId: Int = 111

  override def postForEntity[T](url: String, request: Any, responseType: Class[T], uriVariables: Object*): ResponseEntity[T] = {
    scheduleCount += 1
    val id: Int = nextQueryId
    nextQueryId += 1
    new ResponseEntity(QueryScheduleResult(id, "", "", "STRUCTURE", null).asInstanceOf[T], HttpStatus.OK)
  }

  override def getForEntity[T](url: String, responseType: Class[T], uriVariables: Object*): ResponseEntity[T] = {
    val kingdom: Classification = Classification("Organic compounds", null, null, null)
    val entity: ClassyfireResult =
      ClassyfireResult(null, "InChIKey=HEFYAORHSGZUIX-UHFFFAOYSA-N", kingdom, null, null, null, null, null, null, null, null, null, null, null, null, null)
    new ResponseEntity(QueryResult(111, "", "Done", null, Array(entity)).asInstanceOf[T], HttpStatus.OK)
  }
}
