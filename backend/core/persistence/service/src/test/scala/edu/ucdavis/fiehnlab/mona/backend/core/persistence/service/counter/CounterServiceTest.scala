package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.counter

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.CounterMongoRepository
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
class CounterServiceTest extends WordSpec with LazyLogging {

  @Autowired
  val counterService: CounterService = null

  @Autowired
  val counterRepository: CounterMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CounterServiceTest" must {

    counterRepository.deleteAll()

    "create a new counter" in {
      val result = counterService.getNextCounterValue("spectrumID")
      assert(result != null)
      assert(result.count == 1)
    }

    "increment the counter" in {
      (2 to 10).foreach(i => assert(counterService.getNextCounterValue("spectrumID").count == i))
    }

    "create another counter" in {
      val result = counterService.getNextCounterValue("otherCounter")
      assert(result != null)
      assert(result.count == 1)
    }
  }
}

@SpringBootApplication
class TestConfig