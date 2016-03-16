package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.service

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.EmbeddedRestServerConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.util.Properties


/**
  * Created by wohlg on 3/15/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedRestServerConfig]))
class SpectrumPersistenceServiceTest extends WordSpec {
  val keepRunning = Properties.envOrElse("keep.server.running", "false").toBoolean

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null


  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  "a spectrum persistence service " must {

    "ensure we start with an empty repository" in {
      assert(spectrumPersistenceService.count() == 0)
    }

    "have at least one listener assigned " in {
      assert(spectrumPersistenceService.persistenceEventListeners.size() != 0)
    }

    s"store ${exampleRecords.length} records" in {
      exampleRecords.foreach { spectrum =>
        val countBefore = spectrumPersistenceService.count()
        spectrumPersistenceService.add(spectrum)
        assert(spectrumPersistenceService.count() == countBefore + 1)
      }

      assert(spectrumPersistenceService.count() == exampleRecords.length)
    }

    "query all data" in {
      val result = spectrumPersistenceService.query().iterator

      var count = 0
      while (result.hasNext) {
        count = count + 1
        result.next()
      }

      assert(count == exampleRecords.length)
    }
    "query data with the query tags=q='text==LCMS'" in {
      val result = spectrumPersistenceService.query("tags=q='text==LCMS'").iterator

      var count = 0
      while (result.hasNext) {
        count = count + 1
        result.next()
      }

      assert(count == exampleRecords.length)
    }

    "query data with the query metaData=q='name==\"ion mode\" and value==positive'" in {
      val result = spectrumPersistenceService.query("""metaData=q='name=="ion mode" and value==positive'""").iterator

      var count = 0
      while (result.hasNext) {
        count = count + 1
        result.next()
      }

      assert(count == 33)
    }


    "query data with the query metaData=q='name==\"ion mode\" and value==negative'" in {
      val result = spectrumPersistenceService.query("""metaData=q='name=="ion mode" and value==negative'""").iterator

      var count = 0
      while (result.hasNext) {
        count = count + 1
        result.next()
      }

      assert(count == 25)
    }


    "query data with pagination" in {
      val result = spectrumPersistenceService.query("""metaData=q='name=="ion mode" and value==negative'""", new PageRequest(0, 10))
      assert(result.getTotalPages == 3)
      assert(result.getContent.size() == 10)
    }

    "update data" in {

      val countBefore = spectrumPersistenceService.count()
      val spectrum: Spectrum = spectrumPersistenceService.query().iterator.next()

      val toUpdate = spectrum.copy(spectrum = "1:1")

      assert(spectrum.id == toUpdate.id)
      spectrumPersistenceService.update(toUpdate)

      val updated = spectrumPersistenceService.get(spectrum.id)

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

    "if specified the server should stay online, this can be done using the env variable 'keep.server.running=true' " in {
      if (keepRunning) {
        while (keepRunning) {
          Thread.sleep(300000); // Every 5 minutes
        }
      }
    }
  }
}
