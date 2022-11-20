package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.listener.AkkaEventScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumRepository
import org.hibernate.Hibernate
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

import java.io.InputStreamReader
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.jdk.CollectionConverters._
import scala.collection.mutable.ListBuffer

@SpringBootTest(classes = Array(classOf[TestConfig]))
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SpectrumPersistenceServiceWithAkkaHandlerTest extends AnyWordSpec with LazyLogging with Eventually with BeforeAndAfterEach{

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val spectrumRepository: SpectrumRepository = null

  @Autowired
  val eventCounter: ReceivedEventCounter[Spectrum] = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  @Autowired private val transactionManager: PlatformTransactionManager = null

  private var transactionTemplate: TransactionTemplate = null

  val testRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")), new TypeReference[Array[Spectrum]] {})

  new TestContextManager(this.getClass).prepareTestInstance(this)

  protected override def beforeEach() = (
    transactionTemplate = new TransactionTemplate(transactionManager)
    )

  protected override def afterEach() = (
    transactionTemplate = null
    )

  "a spectrum persistence service " must {
    val exampleRecords = testRecords.toList

    "delete everything" in {
      transactionTemplate.execute{ x =>
        spectrumRepository.deleteAll()
        Hibernate.initialize()
        x
      }

    }

    "scheduler must be of type AkkaEventScheduler" in {
      assert(spectrumPersistenceService.eventScheduler.isInstanceOf[AkkaEventScheduler[Spectrum]])
    }

    "ensure we start with an empty repository" in {
      val count = transactionTemplate.execute{ x=>
        val z = spectrumPersistenceService.count()
        Hibernate.initialize(z)
        z
      }
      assert(count == 0)
    }


    List(1).foreach { iteration =>
      s"we run every test several times, since we have caching, this one is iteration $iteration" should {

        "have at least one listener assigned " in {
          assert(spectrumPersistenceService.eventScheduler.persistenceEventListeners.size() >= 1)
        }

        s"store ${exampleRecords.length} records" in {
          transactionTemplate.execute{ x =>
            spectrumPersistenceService.saveAll(exampleRecords.asJava)
            Hibernate.initialize()
            x
          }


          //this can happen async in the background so we need to wrap it with an eventually
          eventually(timeout(10 seconds)) {
            val countPS = transactionTemplate.execute { x=>
              val z = spectrumPersistenceService.count()
              Hibernate.initialize(z)
              z
            }

            val countR = transactionTemplate.execute { x =>
              val z = spectrumRepository.count()
              Hibernate.initialize(z)
              z
            }
            assert(countPS == exampleRecords.length)
            assert(countR == countPS)
          }
        }

        "there should have been some event's been send to the event bus " in {
          logger.info(s"${eventCounter.getEventCount}")
          eventually(timeout(10 seconds)) {
            assert(eventCounter.getEventCount >= exampleRecords.length)
          }
        }

        "query all data" in {
          val result: Iterable[Spectrum] = transactionTemplate.execute { x =>
            val r = spectrumPersistenceService.findAll()
            Hibernate.initialize(r)
            r.asScala
          }
          assert(result.size == exampleRecords.length)
        }

        "query all data with pagination " in {
          val result: Page[Spectrum] = transactionTemplate.execute { x =>
            val r = spectrumPersistenceService.findAll(PageRequest.of(0, 10))
            Hibernate.initialize(r)
            r
          }
          assert(result.getTotalPages == 6)
        }

        s"query data with the query tags.text:'LCMS'" in {
          val result: Long = transactionTemplate.execute { x =>
            val r = spectrumPersistenceService.count("tags.text:'LCMS'")
            Hibernate.initialize(r)
            r
          }
          assert(result == 58)
        }

        "query data with the query tags.text:LCMS" in {
          val result: Long = transactionTemplate.execute { x =>
            val r = spectrumPersistenceService.count("tags.text:'LCMS'")
            Hibernate.initialize(r)
            r
          }
          assert(result == 58)
        }

        s"query data with the query metaData.name:'ion mode' and metaData.value:'positive'" in {
          val result: Long = transactionTemplate.execute { x =>
            val r = spectrumPersistenceService.count("metaData.name:'ion mode' and metaData.value:'positive'")
            Hibernate.initialize(r)
            r
          }
          assert(result == 33)
        }

        s"query data with the query metaData.name:'ionization mode' and metaData.value:'negative' and compound.metaData.name in ('molecular formula','InChIKey') and compound.metaData.value in ('C6H4N4O2','UYEUUXMDVNYCAM-UHFFFAOYSA-N')" in {
          //metaData.name~'ionization mode' and metaData.value~'negative' and compound.metaData.name~'molecular formula' and compound.metaData.value~'C6H4N4O2' and
          val result: Long = transactionTemplate.execute { x =>
            val r = spectrumPersistenceService.count("metaData.name:'ionization mode' and metaData.value:'negative' and compound.metaData.name in ('molecular formula','InChIKey') and compound.metaData.value in ('C6H4N4O2','UYEUUXMDVNYCAM-UHFFFAOYSA-N')")
            Hibernate.initialize(r)
            r
          }
          assert(result == 1)
        }

        "query data with the query metaData.name:'ion mode' and metaData.value:'negative'" in {
          val result: Long = transactionTemplate.execute { x =>
            val r = spectrumPersistenceService.count("metaData.name:'ion mode' and metaData.value:'negative'")
            Hibernate.initialize(r)
            r
          }
          assert(result == 25)
        }

        "query data with pagination" in {
          val result: Page[Spectrum] = transactionTemplate.execute { x =>
            val r = spectrumPersistenceService.findAll("metaData.name:'ion mode' and metaData.value:'positive'", PageRequest.of(0, 10))
            Hibernate.initialize(r)
            r
          }
          logger.info(s"# of pages ${result.getTotalPages}")
          assert(result.getContent.size() == 10)
          assert(result.getContent.get(0).getId == "3471394")
        }

        "query data with pagination page 2" in {
          val result: Page[Spectrum] = transactionTemplate.execute { x =>
            val r = spectrumPersistenceService.findAll("metaData.name:'ion mode' and metaData.value:'positive'", PageRequest.of(1, 10))
            Hibernate.initialize(r)
            r
          }
          logger.info(s"# of pages ${result.getTotalPages}")
          assert(result.getContent.size() == 10)
          assert(result.getContent.get(0).getId == "3475854")
        }

        "update data" in {
          val countBefore: Long = transactionTemplate.execute { x =>
            val c = spectrumPersistenceService.count()
            Hibernate.initialize(c)
            c
          }
          val spectrum: Spectrum= transactionTemplate.execute { x =>
            val s = spectrumPersistenceService.findAll()
            Hibernate.initialize(s)
            s.iterator().next()
          }
          val toUpdate: Spectrum = transactionTemplate.execute { x =>
            val s = spectrumRepository.findById(spectrum.getId).get()
            Hibernate.initialize(s)
            s
          }
          assert(spectrum.getId == toUpdate.getId)

          transactionTemplate.execute { x =>
            val s = spectrumRepository.findById(spectrum.getId).get()
            spectrumRepository.saveAndFlush(s)
            Hibernate.initialize(s)
            s
          }

          val count: Long = transactionTemplate.execute{ x =>
            val c = spectrumPersistenceService.count()
            Hibernate.initialize(c)
            c
          }
          assert(countBefore == count)
        }

        "present us with a count for data in the repository" in {
          var count: Long = 0
          transactionTemplate.execute { x=>
            count = spectrumPersistenceService.count()
            count
          }
          assert(count == exampleRecords.length)
        }

        "present us with a count for specific queries" in {
          val count: Long = transactionTemplate.execute { x =>
            val c = spectrumPersistenceService.count("metaData.name:'ion mode' and metaData.value:'negative'")
            Hibernate.initialize(c)
            c
          }
          assert(count == 25)
        }

        s"we should be able to execute custom queries like compound.names.name:'META-HYDROXYBENZOIC ACID'" in {
          val exampleRecords: Iterable[Spectrum] = transactionTemplate.execute { x =>
            val e = spectrumPersistenceService.findAll("compound.names.name:'META-HYDROXYBENZOIC ACID'").asScala
            Hibernate.initialize(e)
            e
          }
          assert(exampleRecords.toList.size == 1)
        }

        "delete 1 spectra in the repository" in {
          val count: Long = transactionTemplate.execute { x =>
            val c = spectrumPersistenceService.count()
            Hibernate.initialize(c)
            c
          }
          assert(count == exampleRecords.length)
          val spectra: Spectrum = transactionTemplate.execute { x =>
            val s = spectrumPersistenceService.findAll(PageRequest.of(1, 10)).getContent.get(5)
            Hibernate.initialize(s)
            s
          }
          val spectrumResult: Spectrum = transactionTemplate.execute { x =>
            val s = spectrumRepository.findById(spectra.getId).get()
            Hibernate.initialize(s)
            s
          }
          transactionTemplate.execute{ x =>
            spectrumRepository.delete(spectrumResult)
            Hibernate.initialize(spectrumResult)
            spectrumResult
          }


          val deleteCount: Long = transactionTemplate.execute { x =>
            val c = spectrumPersistenceService.count()
            Hibernate.initialize(c)
            c
          }
          eventually(timeout(10 seconds)) {
            assert(deleteCount == count - 1)
          }
        }

        "delete 10 spectra in the repository by utilizing the iterable method" in {
          val ids = ListBuffer[String]()
          val results: Page[Spectrum] = transactionTemplate.execute { x =>
            val r = spectrumPersistenceService.findAll(PageRequest.of(0, 10))
            Hibernate.initialize(r)
            r
          }
            results.getContent.forEach{ spectrum => {
            ids.append(spectrum.getId)
          }}
          val spectrumResults: List[Spectrum] = transactionTemplate.execute { x =>
            val r = spectrumRepository.findAllByIdIn(ids.asJava)
            Hibernate.initialize(r.asScala.toList)
            r.asScala.toList
          }
          val count: Long = transactionTemplate.execute { x =>
            val c = spectrumPersistenceService.count()
            Hibernate.initialize(c)
            c
          }
          transactionTemplate.execute{ x =>
            spectrumPersistenceService.deleteAll(spectrumResults.asJava)
            Hibernate.initialize()
          }


          val countAfter: Long = transactionTemplate.execute{ x =>
            val c = spectrumPersistenceService.count()
            Hibernate.initialize(c)
            c
          }
          eventually(timeout(10 seconds)) {
            assert(countAfter == count - 10)
          }
        }

        "delete all data in the repository" in {
          logger.info(s"spectra before delete ${spectrumPersistenceService.count()}")
          transactionTemplate.execute { x =>
            spectrumPersistenceService.deleteAll()
            Hibernate.initialize()
            x
          }
          val count: Long = transactionTemplate.execute{ x =>
            val c = spectrumPersistenceService.count()
            Hibernate.initialize(c)
            c
          }
          logger.info(s"spectra after delete ${count}")

          eventually(timeout(10 seconds)) {

            assert(count == 0)
          }
        }
      }
    }
  }
}
