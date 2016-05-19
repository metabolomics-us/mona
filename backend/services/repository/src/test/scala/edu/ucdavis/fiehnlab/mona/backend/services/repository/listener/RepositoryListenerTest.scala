package edu.ucdavis.fiehnlab.mona.backend.services.repository.listener

import edu.ucdavis.fiehnlab.mona.backend.services.repository.Repository
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg_000 on 5/18/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[Repository]))
class RepositoryListenerTest extends WordSpec {

  @Autowired
  val repositoryListener:RepositoryListener = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "RepositoryListenerTest" must {

    "be able to receive data and " should {

      "create a file on an add event" in {

      }

      "create a file on an update event" in {

      }

      "delete a file on a delete event" in {

      }
    }
  }
}
