package edu.ucdavis.fiehnlab.mona.backend.core.workflow

import java.io.{InputStreamReader, FileReader}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.config.WorkflowConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.listener.WorkflowListener
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.{Qualifier, Autowired}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.{Import, Bean, Configuration}
import org.springframework.stereotype.Component
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

  @Autowired
  val testListener:TestListener = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  val data = LinearTest("test data")


  "a workflow given data" when {
    "initialized " must {
      "have 3 steps" in {
        assert(workflow.stepSize == 3)
      }

      "must run" in {
        val result = workflow.process(data)
      }

      "the listener must have fired 3 times for finish events" in {
        assert(testListener.processesFinished == 3)
      }

      "the listener must have fired 3 times for start events" in {
        assert(testListener.processesStarted == 3)
      }

    }

    "its graph must have it's elements int the following order" must {
      val graph = workflow.graph

      "first A" in {
        assert(graph.heads.head == graph.getNode("first").get)
      }
      "second B" in {
        assert(graph.getChildren("first").head == graph.getNode("second").get)
      }
      "third C" in {
        assert(graph.getChildren("second").head == graph.getNode("third").get)
      }

    }
  }
}

@Configuration
@Import(Array(classOf[WorkflowConfiguration]))
class TestWorkflowConfig {

  /**
    * defining our workflow
    * @return
    */
  @Bean
  def linearTestWorkflow = new LinearWorkflow[LinearTest]("test-linear")

}

/**
  * test class
  * @param name
  */
case class LinearTest(name: String)

/**
  * example how to define a listener in the code base
  */
@Component
class TestListener extends WorkflowListener[LinearTest] with LazyLogging {
  var processesStarted: Int = 0
  var processesFinished: Int = 0

  /**
    * started processing this step
    *
    * @param value
    * @param step
    */
  override def startedProcessing(value: LinearTest, step: Step): Unit = {
    processesStarted = processesStarted + 1
  }

  /**
    * finished processing with this step
    *
    * @param value
    * @param step
    */
  override def finishedProcessing(value: LinearTest, step: Step): Unit = {
    processesFinished = processesFinished + 1
  }
}

/**
  * define a couple of steps to be executed
  */
@Step(name = "first", description = "first", workflow = "test-linear")
class TestProcessorA extends ItemProcessor[LinearTest, LinearTest] {
  override def process(item: LinearTest): LinearTest = item
}

@Step(name = "second", previousClass = classOf[TestProcessorA], workflow = "test-linear")
class TestProcessorB extends ItemProcessor[LinearTest, LinearTest] {
  override def process(item: LinearTest): LinearTest = item
}

@Step(name = "third", previous = "second", workflow = "test-linear")
class TestProcessorC extends ItemProcessor[LinearTest, LinearTest] {
  override def process(item: LinearTest): LinearTest = item
}
