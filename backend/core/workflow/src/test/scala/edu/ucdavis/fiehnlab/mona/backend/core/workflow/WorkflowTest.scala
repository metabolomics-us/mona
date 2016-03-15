package edu.ucdavis.fiehnlab.mona.backend.core.workflow

import edu.ucdavis.fiehnlab.mona.backend.core.workflow.config.WorkflowConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlgemuth on 3/14/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[WorkflowConfiguration]))
class WorkflowTest extends WordSpec {

  @Autowired
  val workflow:Workflow = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "a workflow given data" when {
    "it should find all beans " should {
      "which have the annotation @Step" in {
        //no idea how to test this...
      }

      "it should build a graph internally" in {
        val graph = workflow.graph


      }
    }
  }
}
