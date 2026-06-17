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

        val ids: java.util.List[String] = transactionTemplate.execute { _ =>
          val page = spectrumPersistenceService.findAll(PageRequest.of(0, 3))
          Hibernate.initialize(page)
          page.getContent.asScala.map(_.getId).asJava
        }
        assert(ids.size == 3)

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
      }
    }
  }
}
