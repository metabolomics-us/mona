package edu.ucdavis.fiehnlab.mona.backend.core.workflow

import java.io.{InputStreamReader, FileReader}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.config.WorkflowConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.{Qualifier, Autowired}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.{Import, Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlgemuth on 3/14/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[TestWorkflowConfig]))
class WorkflowTest extends WordSpec {


  @Autowired
  @Qualifier("linearTestWorkflow")
  val workflow: Workflow[LinearTest] = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  val data = LinearTest("test data")


  "a workflow given data" when {
    "it should find all beans " should {
      "which have the annotation @Step" in {
        assert(workflow.stepSize == 3)
      }

      "it should executed all the steps for the applied data set" in {
        workflow.run(data)
      }

    }

    "its graph must have it's elements int the following order" must {
      val graph = workflow.graph

      "first A" in {
        assert (graph.heads.head == graph.getNode("first").get)
      }
      "second B" in {
        assert (graph.getChildren("first") == graph.getNode("second").get)
      }
      "third C" in {
        assert (graph.getChildren("second") == graph.getNode("third").get)
      }

    }
  }
}

@Configuration
@Import(Array(classOf[WorkflowConfiguration]))
class TestWorkflowConfig {
  @Bean
  def linearTestWorkflow = new Workflow[LinearTest]
}

case class LinearTest(name:String)

@Step(name="first", description = "first")
class TestProcessorA extends ItemProcessor[LinearTest,LinearTest] {
  override def process(item: LinearTest): LinearTest = item
}

@Step(name="second", previousClass = classOf[TestProcessorA])
class TestProcessorB extends ItemProcessor[LinearTest,LinearTest] {
  override def process(item: LinearTest): LinearTest = item
}

@Step(name="third",previousClass = classOf[TestProcessorB])
class TestProcessorC extends ItemProcessor[LinearTest,LinearTest] {
  override def process(item: LinearTest): LinearTest = item
}
