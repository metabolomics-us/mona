package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.counter

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.SequenceMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.config.EmbeddedServiceConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 11/23/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedServiceConfig], classOf[TestConfig]))
class SequenceServiceTest extends WordSpec with LazyLogging {

  @Autowired
  val counterService: SequenceService = null

  @Autowired
  val counterRepository: SequenceMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CounterServiceTest" must {

    counterRepository.deleteAll()

    "create a new counter" in {
      val result = counterService.getNextSequenceValue("spectrumID")
      assert(result != null)
      assert(result.value == 1)
    }

    "increment the counter" in {
      (2 to 10).foreach(i => assert(counterService.getNextSequenceValue("spectrumID").value == i))
    }

    "create another counter" in {
      val result = counterService.getNextSequenceValue("otherCounter")
      assert(result != null)
      assert(result.value == 1)
    }
  }
}

@SpringBootApplication
class TestConfig