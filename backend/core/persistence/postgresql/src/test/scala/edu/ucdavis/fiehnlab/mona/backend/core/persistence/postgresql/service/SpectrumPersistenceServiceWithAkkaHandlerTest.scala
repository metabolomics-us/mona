package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.listener.AkkaEventScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.mat.MaterializedViewRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.SearchTableRepository
import org.scalatest.concurrent.Eventually
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import java.io.InputStreamReader
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.jdk.CollectionConverters._
import scala.collection.mutable.ListBuffer

@SpringBootTest(classes = Array(classOf[TestConfig]))
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SpectrumPersistenceServiceWithAkkaHandlerTest extends AnyWordSpec with LazyLogging with Eventually{

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val spectrumResultRepository: SpectrumResultRepository = null

  @Autowired
  val searchTableRepository: SearchTableRepository = null

  @Autowired
  val matRepository: MaterializedViewRepository = null

  @Autowired
  val eventCounter: ReceivedEventCounter[Spectrum] = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  val testRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")), new TypeReference[Array[Spectrum]] {})

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "a spectrum persistence service " must {
    val tempRecords = ListBuffer[SpectrumResult]()
    testRecords.foreach{ spectrum =>
      tempRecords.append(new SpectrumResult(spectrum.getId, spectrum))
    }
    val exampleRecords = tempRecords.toList

    "delete everything" in {
      spectrumResultRepository.deleteAll()
    }

    "scheduler must be of type AkkaEventScheduler" in {
      assert(spectrumPersistenceService.eventScheduler.isInstanceOf[AkkaEventScheduler[Spectrum]])
    }

    "ensure we start with an empty repository" in {
      assert(spectrumPersistenceService.count() == 0)
    }


    List(1).foreach { iteration =>
      s"we run every test several times, since we have caching, this one is iteration $iteration" should {

        "have at least one listener assigned " in {
          assert(spectrumPersistenceService.eventScheduler.persistenceEventListeners.size() >= 1)
        }

        s"store ${exampleRecords.length} records" in {
          spectrumPersistenceService.saveAll(exampleRecords.asJava)

          //this can happen async in the background so we need to wrap it with an eventually
          eventually(timeout(10 seconds)) {
            assert(spectrumPersistenceService.count() == exampleRecords.length)
            assert(spectrumResultRepository.count() == spectrumPersistenceService.count())
          }
        }

        s"we should be able to create our materialized view" in {

          eventually(timeout(180 seconds)) {
            matRepository.refreshSearchTable()
            logger.info("sleep...")
            assert(searchTableRepository.count() == 59610)
          }

        }

        "there should have been some event's been send to the event bus " in {
          logger.info(s"${eventCounter.getEventCount}")
          eventually(timeout(10 seconds)) {
            assert(eventCounter.getEventCount >= exampleRecords.length)
          }
        }

        "query all data" in {
          val result = spectrumPersistenceService.findAll()
          assert(result.asScala.size == exampleRecords.length)
        }

        "query all data with pagination " in {
          val result: Page[SpectrumResult] = spectrumPersistenceService.findAll(PageRequest.of(0, 10))
          assert(result.getTotalPages == 6)
        }

        s"query data with the query text==\"LCMS\"" in {
          val result: Long = spectrumPersistenceService.count("text==\"LCMS\"")
          assert(result == 58)
        }

        "query data with the query text==LCMS" in {
          val result: Long = spectrumPersistenceService.count("text==LCMS")
          assert(result == 58)
        }

        s"query data with the query metadataName==\"ion mode\" and metadataValue==positive" in {
          val result: Long = spectrumPersistenceService.count("metadataName==\"ion mode\" and metadataValue==positive")
          assert(result == 33)
        }

        "query data with the query metaData=q='name==\"ion mode\" and value==negative'" in {
          val result: Long = spectrumPersistenceService.count("metadataName==\"ion mode\" and metadataValue==negative")
          assert(result == 25)
        }

        "query data with pagination" in {
          val result = spectrumPersistenceService.findAll("metadataName==\"ion mode\" and metadataValue==positive", PageRequest.of(0, 10))
          logger.info(s"# of pages ${result.getTotalPages}")
          assert(result.getContent.size() == 10)
          assert(result.getContent.get(0).getMonaId == "3472824")
        }

        "query data with pagination page 2" in {
          val result = spectrumPersistenceService.findAll("metadataName==\"ion mode\" and metadataValue==positive", PageRequest.of(1, 10))
          logger.info(s"# of pages ${result.getTotalPages}")
          assert(result.getContent.size() == 10)
          assert(result.getContent.get(0).getMonaId == "3477809")
        }

        "update data" in {
          val countBefore = spectrumPersistenceService.count()
          val spectrum: SpectrumResult = spectrumPersistenceService.findAll().iterator.next
          val toUpdate: SpectrumResult = spectrumResultRepository.findByMonaId(spectrum.getMonaId)
          assert(spectrum.getMonaId == toUpdate.getMonaId)

          spectrumPersistenceService.update(toUpdate)

          assert(countBefore == spectrumPersistenceService.count())
        }

        "present us with a count for data in the repository" in {
          assert(spectrumPersistenceService.count() == exampleRecords.length)
        }

        "present us with a count for specific queries" in {
          assert(spectrumPersistenceService.count("metadataName==\"ion mode\" and metadataValue==negative") == 25)
        }

        s"we should be able to execute custom queries like metadataValue==\"META-HYDROXYBENZOIC ACID\"" in {
          val exampleRecords = spectrumPersistenceService.findAll("metadataValue==\"META-HYDROXYBENZOIC ACID\"")
          assert(exampleRecords.asScala.toList.size == 1)
        }

        "delete 1 spectra in the repository" in {
          assert(spectrumPersistenceService.count() == exampleRecords.length)
          val spectra: SpectrumResult = spectrumPersistenceService.findAll(PageRequest.of(1, 10)).getContent.get(5)
          val count = spectrumPersistenceService.count()
          val spectrumResult: SpectrumResult = spectrumResultRepository.findByMonaId(spectra.getMonaId)
          spectrumPersistenceService.delete(spectrumResult)

          eventually(timeout(10 seconds)) {
            assert(spectrumPersistenceService.count() == count - 1)
          }
        }

        "delete 10 spectra in the repository by utilizing the iterable method" in {
          val ids = ListBuffer[String]()
          spectrumPersistenceService.findAll(PageRequest.of(0, 10)).getContent.forEach{ spectrum => {
            ids.append(spectrum.getMonaId)
          }}
          val spectrumResults = spectrumResultRepository.findAllByMonaIdIn(ids.asJava)
          val count = spectrumPersistenceService.count()
          spectrumPersistenceService.deleteAll(spectrumResults)


          eventually(timeout(10 seconds)) {
            assert(spectrumPersistenceService.count() == count - 10)
          }
        }

        "delete all data in the repository" in {
          logger.info(s"spectra before delete ${spectrumPersistenceService.count()}")
          spectrumPersistenceService.deleteAll()
          logger.info(s"spectra after delete ${spectrumPersistenceService.count()}")

          eventually(timeout(10 seconds)) {

            assert(spectrumPersistenceService.count() == 0)
          }
        }
      }
    }
  }
}
