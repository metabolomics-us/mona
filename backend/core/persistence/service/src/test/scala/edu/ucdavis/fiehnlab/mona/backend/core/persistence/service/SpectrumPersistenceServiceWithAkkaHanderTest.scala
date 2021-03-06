package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service

import java.io.InputStreamReader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.config.EmbeddedServiceConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.listener.AkkaEventScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Properties

/**
  * Created by wohlg on 3/15/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[EmbeddedServiceConfig]))
class SpectrumPersistenceServiceWithAkkaHanderTest extends WordSpec with LazyLogging with Eventually {

  val keepRunning: Boolean = Properties.envOrElse("keep.server.running", "false").toBoolean

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val spectrumElasticRepository: ISpectrumElasticRepositoryCustom = null

  @Autowired
  val eventCounter: ReceivedEventCounter[Spectrum] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "a spectrum persistence service " must {
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))
    val curatedRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))

    "delete everything" in {
      spectrumElasticRepository.deleteAll()
      spectrumMongoRepository.deleteAll()
    }

    "scheduler must be of type AkkaEventScheduler" in {
      assert(spectrumPersistenceService.eventScheduler.isInstanceOf[AkkaEventScheduler[Spectrum]])
    }

    "ensure we start with an empty repository" in {
      assert(spectrumPersistenceService.count() == 0)
    }


    List(1, 2, 3).foreach { iteration =>
      s"we run every test several times, since we have caching, this one is iteration $iteration" should {

        "have at least one listener assigned " in {
          assert(spectrumPersistenceService.eventScheduler.persistenceEventListeners.size() > 1)
        }

        s"store ${exampleRecords.length} records" in {
          spectrumPersistenceService.save(exampleRecords.toList.asJava)

          //this can happen async in the background so we need to wrap it with an eventually
          eventually(timeout(10 seconds)) {
            assert(spectrumPersistenceService.count() == exampleRecords.length)
            assert(spectrumMongoRepository.count() == spectrumElasticRepository.count())
          }
        }

        "there should have been some event's been send to the event bus " in {
          eventually(timeout(10 seconds)) {
            assert(eventCounter.getEventCount >= exampleRecords.length)
          }
        }

        "query all data" in {
          val result = spectrumPersistenceService.findAll()
          assert(result.asScala.size == exampleRecords.length)
        }

        "query all data with pagination " in {
          val result: Page[Spectrum] = spectrumPersistenceService.findAll(new PageRequest(0, 10))
          assert(result.getTotalPages == 6)
        }

        "query data with the query tags=q='text==LCMS'" in {
          val result: Array[Spectrum] = spectrumPersistenceService.findAll("""tags=q='text==LCMS'""", "").asScala.toArray
          assert(result.map(_.id).toSet.size == exampleRecords.length)
          assert(result.length == exampleRecords.length)
        }

        "query data with the query tags.text==LCMS" in {
          val result: Array[Spectrum] = spectrumPersistenceService.findAll("""tags.text==LCMS""", "").asScala.toArray
          assert(result.map(_.id).toSet.size == exampleRecords.length)
          assert(result.length == exampleRecords.length)
        }

        "query data with the query metaData=q='name==\"ion mode\" and value==positive'" in {
          val result: Array[Spectrum] = spectrumPersistenceService.findAll("""metaData=q='name=="ion mode" and value==positive'""", "").asScala.toArray
          assert(result.map(_.id).toSet.size == 33)
          assert(result.length == 33)
        }

        "query data with the query metaData=q='name==\"ion mode\" and value==negative'" in {
          val result: Array[Spectrum] = spectrumPersistenceService.findAll("""metaData=q='name=="ion mode" and value==negative'""", "").asScala.toArray
          assert(result.map(_.id).toSet.size == 25)
          assert(result.length == 25)
        }

        "query data with pagination" in {
          val result = spectrumPersistenceService.findAll("""metaData=q='name=="ion mode" and value==negative'""", "", new PageRequest(0, 10))
          assert(result.getTotalPages == 3)
          assert(result.getContent.size() == 10)
        }

        "update data" in {
          val countBefore = spectrumPersistenceService.count()
          val spectrum: Spectrum = spectrumPersistenceService.findAll().iterator.next()

          val toUpdate = spectrum.copy(spectrum = "1:1")
          assert(spectrum.id == toUpdate.id)

          spectrumPersistenceService.update(toUpdate)
          val updated = spectrumPersistenceService.findOne(spectrum.id)

          assert(updated.spectrum == "1:1")
          assert(countBefore == spectrumPersistenceService.count())
        }

        "present us with a count for data in the repository" in {
          assert(spectrumPersistenceService.count() == exampleRecords.length)
        }

        "present us with a count for specific queries" in {
          assert(spectrumPersistenceService.count("metaData=q='name==\"ion mode\" and value==negative'", "") == 25)
        }

        "we should be able to execute custom queries like compound.names.name=='META-HYDROXYBENZOIC ACID'" ignore {
          val exampleRecords = spectrumPersistenceService.findAll("""compound.names.name=='META-HYDROXYBENZOIC ACID'""", "")
          assert(exampleRecords.asScala.toList.size == 1)
        }

        "we should be able to execute custom subqueries like compound names.name=='META-HYDROXYBENZOIC ACID'" in {
          val exampleRecords = spectrumPersistenceService.findAll("""compound=q="names.name=='META-HYDROXYBENZOIC ACID'"""", "")
          assert(exampleRecords.asScala.toList.size == 1)
        }

        "delete 1 spectra in the repository" in {
          assert(spectrumPersistenceService.count() == exampleRecords.length)
          val spectra: Spectrum = spectrumPersistenceService.findAll(new PageRequest(1, 10)).getContent.get(5)
          val count = spectrumPersistenceService.count()
          spectrumPersistenceService.delete(spectra)

          eventually(timeout(10 seconds)) {
            assert(spectrumPersistenceService.count() == count - 1)
            assert(spectrumMongoRepository.count() == count - 1)
            assert(spectrumElasticRepository.count() == count - 1)
          }
        }

        "delete 10 spectra in the repository by utilizing the iterable method" in {
          val spectra = spectrumPersistenceService.findAll(new PageRequest(0, 10)).getContent
          val count = spectrumPersistenceService.count()
          spectrumPersistenceService.delete(spectra)

          //assert(spectrumMongoRepository.count() == spectrumElasticRepository.count())

          eventually(timeout(10 seconds)) {
            assert(spectrumPersistenceService.count() == count - 10)
            assert(spectrumMongoRepository.count() == count - 10)
            assert(spectrumElasticRepository.count() == count - 10)
          }
        }

        "delete all data in the repository" in {
          logger.info(s"spectra before delete ${spectrumPersistenceService.count()}")
          spectrumPersistenceService.deleteAll()
          logger.info(s"spectra after delete ${spectrumPersistenceService.count()}")

          eventually(timeout(10 seconds)) {
            logger.info(s"spectra after delete mongo ${spectrumMongoRepository.count()}")
            logger.info(s"spectra after delete elastic ${spectrumElasticRepository.count()}")

            assert(spectrumPersistenceService.count() == 0)
            assert(spectrumMongoRepository.count() == 0)
            assert(spectrumElasticRepository.count() == 0)
          }
        }

        "if specified the server should stay online, this can be done using the env variable 'keep.server.running=true' " in {
          if (keepRunning) {
            while (keepRunning) {
              Thread.sleep(300000); // Every 5 minutes
            }
          }
        }
      }
    }
  }
}