package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.counter

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.SequenceMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.config.EmbeddedServiceConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 11/23/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[EmbeddedServiceConfig], classOf[TestConfig]))
class SequenceServiceTest extends WordSpec with LazyLogging {

  @Autowired
  val counterService: SequenceService = null

  @Autowired
  val counterRepository: SequenceMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CounterServiceTest" must {

    counterRepository.deleteAll()

    "create another counter" in {
      val result = counterService.getNextSequenceValue("counter")
      assert(result != null)
      assert(result.value == 1)
    }

    "increment the counter" in {
      (2 to 10).foreach(i => assert(counterService.getNextSequenceValue("counter").value == i))
    }

    "create a new MoNA ID counter" in {
      (1 to 5).foreach(i => assert(counterService.getNextMoNAID == s"MoNA00000$i"))
    }

    "create a news ID counter" in {
      (1 to 10).foreach(i => assert(counterService.getNextNewsID == i.toString))
    }
  }
}

@SpringBootApplication
class TestConfig