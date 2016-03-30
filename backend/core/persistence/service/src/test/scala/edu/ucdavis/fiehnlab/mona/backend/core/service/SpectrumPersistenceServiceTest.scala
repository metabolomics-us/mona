package edu.ucdavis.fiehnlab.mona.backend.core.service

import java.io.InputStreamReader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.service.persistence.SpectrumPersistenceService
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._
import scala.util.Properties


/**
  * Created by wohlg on 3/15/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedServiceConfig]))
class SpectrumPersistenceServiceTest extends WordSpec with LazyLogging{
  val keepRunning = Properties.envOrElse("keep.server.running", "false").toBoolean

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val spectrumElasticRepository: ISpectrumElasticRepositoryCustom = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  "a spectrum persistence service " must {

    "ensure we start with an empty repository" in {
      assert(spectrumPersistenceService.count() == 0)
    }

    "have at least one listener assigned " in {
      assert(spectrumPersistenceService.eventScheduler.persistenceEventListeners.size() != 0)
    }

    s"store ${exampleRecords.length} records" in {
      spectrumPersistenceService.save(exampleRecords.toList.asJava)
      assert(spectrumPersistenceService.count() == exampleRecords.length)
      assert(spectrumMongoRepository.count() == spectrumElasticRepository.count())
    }

    "query all data" in {
      val result = spectrumPersistenceService.findAll().iterator

      var count = 0
      while (result.hasNext) {
        count = count + 1
        result.next()
      }

      assert(count == exampleRecords.length)
    }

    "query all data with pagination " in {
      val result: Page[Spectrum] = spectrumPersistenceService.findAll(new PageRequest(0, 10))
      assert(result.getTotalPages() == 6)
    }

    "query data with the query tags=q='text==LCMS'" in {
      val result = spectrumPersistenceService.findAll("tags=q='text==LCMS'").iterator

      var count = 0
      while (result.hasNext) {
        count = count + 1
        result.next()
      }

      assert(count == exampleRecords.length)
    }

    "query data with the query metaData=q='name==\"ion mode\" and value==positive'" in {
      val result = spectrumPersistenceService.findAll("""metaData=q='name=="ion mode" and value==positive'""").iterator

      var count = 0
      while (result.hasNext) {
        count = count + 1
        result.next()
      }

      assert(count == 33)
    }


    "query data with the query metaData=q='name==\"ion mode\" and value==negative'" in {
      val result = spectrumPersistenceService.findAll("""metaData=q='name=="ion mode" and value==negative'""").iterator

      var count = 0
      while (result.hasNext) {
        count = count + 1
        result.next()
      }

      assert(count == 25)
    }


    "query data with pagination" in {
      val result = spectrumPersistenceService.findAll("""metaData=q='name=="ion mode" and value==negative'""", new PageRequest(0, 10))
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

      val countAfter = spectrumPersistenceService.count()

      assert(countAfter == countBefore)
    }

    "present us with a count for data in the repository" in {
      assert(spectrumPersistenceService.count() == exampleRecords.length)
    }

    "present us with a count for specific queries" in {
      assert(spectrumPersistenceService.count("metaData=q='name==\"ion mode\" and value==negative'") == 25)
    }

    "delete 1 spectra in the repository" in {
      assert(spectrumPersistenceService.count() == exampleRecords.length)
      val spectra: Spectrum = spectrumPersistenceService.findAll(new PageRequest(1, 10)).getContent.get((5))
      val count = spectrumPersistenceService.count()
      spectrumPersistenceService.delete(spectra)

      assert(spectrumPersistenceService.count()  == count - 1)
    }

    "delete 10 spectra in the repository by utilizing the iterable method" in {
      val spectra = spectrumPersistenceService.findAll(new PageRequest(0, 10)).getContent
      val count = spectrumPersistenceService.count()
      spectrumPersistenceService.delete(spectra)

      //assert(spectrumMongoRepository.count() == spectrumElasticRepository.count())
      assert(spectrumPersistenceService.count() == count -10)
    }

    "delete all data in the repository" in {
      logger.info(s"spectra before delete ${spectrumPersistenceService.count()}")
      spectrumPersistenceService.deleteAll()
      logger.info(s"spectra after delete ${spectrumPersistenceService.count()}")
      assert(spectrumPersistenceService.count() == 0)
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
