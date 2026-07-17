package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{DeletionJob, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{DeletionJobRepository, SpectrumRepository}
import org.hibernate.Hibernate
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

import javax.persistence.EntityManager
import java.io.InputStreamReader
import java.util.{Date, UUID}
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
  * Tests the batched, progress-tracked deletion paths added for the library deletion refactor.
  * Verifies that a query based delete removes exactly the matching spectra (and nothing else),
  * that an id based delete removes the requested spectra, and that the DeletionJob progress
  * counters are recorded
  */
@SpringBootTest(classes = Array(classOf[TestConfig]))
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SpectrumPersistenceServiceDeletionTest extends AnyWordSpec with LazyLogging with Eventually with BeforeAndAfterAll {

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val spectrumRepository: SpectrumRepository = null

  @Autowired
  val deletionJobRepository: DeletionJobRepository = null

  @Autowired
  val monaMapper: ObjectMapper = MonaMapper.create

  @Autowired private val transactionManager: PlatformTransactionManager = null

  @Autowired private val entityManager: EntityManager = null

  private var transactionTemplate: TransactionTemplate = null

  val testRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")), new TypeReference[Array[Spectrum]] {})

  new TestContextManager(this.getClass).prepareTestInstance(this)

  private def countAll(): Long = transactionTemplate.execute { _ =>
    val c = spectrumPersistenceService.count()
    Hibernate.initialize(c)
    c
  }

  private def countQuery(query: String): Long = transactionTemplate.execute { _ =>
    val c = spectrumPersistenceService.count(query)
    Hibernate.initialize(c)
    c
  }

  // score/splash/submitter/library ids live on the spectrum row itself, so they must be captured
  // before a delete, then checked for absence afterward via a direct count against their own table
  private case class AssociationIds(scoreIds: java.util.List[Object], splashIds: java.util.List[Object],
                                     submitterIds: java.util.List[Object], libraryIds: java.util.List[Object])

  private def associationIds(ids: java.util.List[String]): AssociationIds = transactionTemplate.execute { _ =>
    val rows = spectrumRepository.findAssociationIdsByIdIn(ids).asScala
    def column(index: Int): java.util.List[Object] = rows.flatMap(row => Option(row(index))).asJava
    AssociationIds(column(1), column(2), column(3), column(4))
  }

  // not every fixture spectrum has a score/splash/submitter set (they're nullable), so picking an
  // arbitrary page of spectra risks a meaningless orphan-cleanup assertion. This finds ids among a
  // candidate pool that actually have all three populated
  private def idsWithScoreSplashAndSubmitter(candidates: java.util.List[String], count: Int): java.util.List[String] =
    transactionTemplate.execute { _ =>
      spectrumRepository.findAssociationIdsByIdIn(candidates).asScala
        .collect { case row if row(1) != null && row(2) != null && row(3) != null => row(0).asInstanceOf[String] }
        .take(count)
        .asJava
    }

  private def countRemaining(entityName: String, ids: java.util.List[Object]): Long = transactionTemplate.execute { _ =>
    if (ids.isEmpty) 0L
    // classOf[Long] here would resolve to the JVM primitive long, not java.lang.Long, which
    // JPA's typed query requires
    else entityManager.createQuery(s"SELECT COUNT(x) FROM $entityName x WHERE x.id IN :ids", classOf[java.lang.Long]).setParameter("ids", ids).getSingleResult.longValue()
  }

  protected override def beforeAll(): Unit = {
    transactionTemplate = new TransactionTemplate(transactionManager)

    // start from a clean, fully populated repository
    transactionTemplate.execute { _ =>
      spectrumRepository.deleteAll()
      Hibernate.initialize()
    }
    transactionTemplate.execute { _ =>
      spectrumPersistenceService.saveAll(testRecords.toList.asJava)
      Hibernate.initialize()
    }

    eventually(timeout(15 seconds)) {
      assert(countAll() == testRecords.length)
    }
  }

  "the tracked spectrum deletion" when {
    val query = "metaData.name:'ion mode' and metaData.value:'negative'"

    "deleting by query" should {
      "remove exactly the matching spectra and record progress on the job" in {
        val totalBefore = countAll()
        val matchedBefore = countQuery(query)

        // the query must be selective for this test to mean anything
        assert(matchedBefore > 0)
        assert(matchedBefore < totalBefore)

        val job = new DeletionJob(UUID.randomUUID.toString, query, null, "test@test.com", new Date, DeletionJob.STATUS_SCHEDULED, matchedBefore)
        deletionJobRepository.save(job)

        spectrumPersistenceService.deleteSpectraByQueryTracked(query, job)

        eventually(timeout(20 seconds)) {
          assert(countQuery(query) == 0)
          assert(countAll() == totalBefore - matchedBefore)
        }

        // progress counters on the job reflect what happened
        assert(job.getDeleted == matchedBefore)
        assert(job.getSkipped == 0)

        // the persisted job row is updated too
        val persisted = deletionJobRepository.findById(job.getId).orElse(null)
        assert(persisted != null)
        assert(persisted.getDeleted == matchedBefore)
      }
    }

    "deleting by id" should {
      "remove only the requested spectra" in {
        val totalBefore = countAll()

        val candidates: java.util.List[String] = transactionTemplate.execute { _ =>
          val page = spectrumPersistenceService.findAll(PageRequest.of(0, 20))
          Hibernate.initialize(page)
          page.getContent.asScala.map(_.getId).asJava
        }

        // captured before the delete: score/splash/submitter ids live on the spectrum row itself,
        // so a bulk delete of spectrum rows can't cascade to them at the database level like it
        // does for compound/metaData/tags. Confirming they're really gone (not just orphaned) is
        // the regression test for that -- picked from a pool since not every fixture spectrum has
        // all three set
        val ids = idsWithScoreSplashAndSubmitter(candidates, 3)
        assert(ids.size == 3)

        val associations = associationIds(ids)
        assert(!associations.scoreIds.isEmpty)
        assert(!associations.splashIds.isEmpty)
        assert(!associations.submitterIds.isEmpty)

        val job = new DeletionJob(UUID.randomUUID.toString, null, ids.asScala.mkString(","), "test@test.com", new Date, DeletionJob.STATUS_SCHEDULED, ids.size.toLong)
        deletionJobRepository.save(job)

        spectrumPersistenceService.deleteSpectraByIdsTracked(ids, job)

        eventually(timeout(20 seconds)) {
          assert(countAll() == totalBefore - 3)
          ids.asScala.foreach { id =>
            assert(!spectrumRepository.existsById(id))
          }
        }

        assert(job.getDeleted == 3)
        assert(job.getSkipped == 0)

        assert(countRemaining("Score", associations.scoreIds) == 0)
        assert(countRemaining("Splash", associations.splashIds) == 0)
        assert(countRemaining("SpectrumSubmitter", associations.submitterIds) == 0)
        assert(countRemaining("Library", associations.libraryIds) == 0)
      }
    }
  }
}
