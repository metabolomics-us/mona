package edu.ucdavis.fiehnlab.mona.backend.core.workflow

import edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph.ProcessingStep
import org.scalatest.WordSpec

/**
  * Created by wohlgemuth on 4/20/16.
  */
class WorkflowBuilderTest extends WordSpec {

  /**
    * completely manual approach of creating the processing steps and
    * they link to each other based on the defined order. No annotations are
    * utilized
    */
  "build - based on Processing steps" must {
    val workflowBuilder = new WorkflowBuilder[LinearTest]

    "assemble 3 steps" in {
      workflowBuilder.enableAnnotationLinking(false)
        .add(ProcessingStep("first", new TestProcessorA, "none"))
        .add(ProcessingStep("second", new TestProcessorB, "none"))
        .add(ProcessingStep("third", new TestProcessorC, "none"))
        .add(new TestListener)
        .forceLinear(true)

    }

    "build the workflow" should {
      val workflow = workflowBuilder.build()

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

      "ensure we have 3 items" in {
        assert(graph.size == 3)
      }
    }
  }

  /**
    * this utilizes manual linking if specified or alternatively tries to connect
    * to other stings by using the internal annotations
    */
  "build - based on ItemProcessors and annotations and Manual linking" must {
    val workflowBuilder = new WorkflowBuilder[LinearTest]


    "assemble 3 steps" in {
      workflowBuilder.enableAnnotationLinking()
        .add(new TestProcessorB, "first") //links to a
        .add(new TestProcessorA)
        .add(new TestProcessorC, "second") // links to b
        .forceLinear(true)

    }

    "build the workflow" should {
      val workflow = workflowBuilder.build()

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
      "ensure we have 3 items" in {
        assert(graph.size == 3)
      }
    }
  }

  /**
    * this will link everything together based on the internally used annotations
    */
  "build - based on ItemProcessors and Annotations" must {
    val workflowBuilder = new WorkflowBuilder[LinearTest]


    "assemble 3 steps" in {
      workflowBuilder.enableAnnotationLinking()
        .add(new TestProcessorB)
        .add(new TestProcessorA)
        .add(new TestProcessorC)
        .forceLinear(true)

    }

    "build the workflow" should {
      val workflow = workflowBuilder.build()

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
      "ensure we have 3 items" in {
        assert(graph.size == 3)
      }
    }


  }

  /**
    * builds the workflow as specified in the order of the given list,
    * while ignoring the parent class properties and linking of the annotations
    */
  "build - based on a List of ItemProcessors" must {
    val workflowBuilder = new WorkflowBuilder[LinearTest]


    "assemble 3 steps" in {

      val items = new TestProcessorA :: new TestProcessorB :: new TestProcessorC :: List()

      workflowBuilder.enableAnnotationLinking(false)
        .add(items)
        .forceLinear(true)

    }

    "build the workflow" should {
      val workflow = workflowBuilder.build()

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
      "ensure we have 3 items" in {
        assert(graph.size == 3)
      }
    }
  }


  /**
    * builds the workflow as specified in the order of the given list,
    * while supporting the parent class properties and linking of the annotations
    */
  "build - based on a List of ItemProcessors with Annotations" must {
    val workflowBuilder = new WorkflowBuilder[LinearTest]


    "assemble 3 steps" in {

      val items = Array(
        new TestProcessorB,
        new TestProcessorC,
        new TestProcessorA
      )

      workflowBuilder.enableAnnotationLinking()
        .add(items)
        .forceLinear(true)

    }

    "build the workflow" should {
      val workflow = workflowBuilder.build()

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
      "ensure we have 3 items" in {
        assert(graph.size == 3)
      }
    }
  }
}
