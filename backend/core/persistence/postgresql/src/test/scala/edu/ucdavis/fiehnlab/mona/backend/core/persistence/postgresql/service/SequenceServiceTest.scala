package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SequenceRepository
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest
@ActiveProfiles(Array("test"))
class SequenceServiceTest extends AnyWordSpec with LazyLogging {

  @Autowired
  val counterService: SequenceService = null

  @Autowired
  val counterRepository: SequenceRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CounterServiceTest" must {

    counterRepository.deleteAll()

    "create another counter" in {
      val result = counterService.getNextSequenceValue("counter")
      assert(result != null)
      assert(result.getValue == 1)
    }

    "increment the counter" in {
      (2 to 10).foreach(i => assert(counterService.getNextSequenceValue("counter").getValue == i))
    }

    "create a new MoNA ID counter" in {
      (1 to 5).foreach(i => assert(counterService.getNextMoNAID == s"MoNA_00000$i"))
    }

    "create a news ID counter" in {
      (1 to 10).foreach(i => assert(counterService.getNextNewsID == i.toString))
    }
  }
}
