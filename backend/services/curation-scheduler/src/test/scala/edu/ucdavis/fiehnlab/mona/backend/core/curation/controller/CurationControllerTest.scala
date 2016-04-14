package edu.ucdavis.fiehnlab.mona.backend.core.curation.controller

import edu.ucdavis.fiehnlab.mona.backend.core.curation.CurrationScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg on 4/13/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[CurrationScheduler]))
class CurationControllerTest extends AbstractSpringControllerTest {

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "CurationControllerTest" should {

    "curateByQuery" in {

    }

    "curateById" in {

    }

    "curateAll" in {

    }

  }
}
