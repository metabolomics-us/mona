package edu.ucdavis.fiehnlab.mona.backend.core.workflow

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.exception.{NameAlreadyRegisteredException, ParentAndParentClassSpecifiedException, RefrenceBeanHasNotBeenAnnotatedException}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph._
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.listener.WorkflowListener
import org.springframework.batch.item.ItemProcessor

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

/**
  * Allows us to easily build workflows in the system
  */
class WorkflowBuilder[TYPE: ClassTag] {

  /**
    * our internal graph to use
    */
  private val graph = new Graph[String, Node[TYPE, TYPE], Edge]

  /**
    * it will only support building linear workflows
    */
  private var linearOnly = true

  /**
    * can processes be automatically linked over annotations
    */
  private var annotationLinking: Boolean = false

  /**
    * internal flag if this builder has been used already
    */
  private var alreadyBuild: Boolean = false

  /**
    * helps us with annotation related parts of the workflow
    */
  private val helper: AnnotationHelper[TYPE] = new AnnotationHelper[TYPE]

  private val listeners: java.util.List[WorkflowListener[TYPE]] = new util.ArrayList[WorkflowListener[TYPE]]()

  /**
    * adds the step and connects it to the last Step in a linear fashion. The user does not have to worry about connectivity
    *
    * @param step
    * @return
    */
  def add(step: ProcessingStep[TYPE, TYPE]): WorkflowBuilder[TYPE] = {
    val node = Node[TYPE, TYPE](step.name, step, step.description)

    if (graph.size == 0) {
      graph.addNode(node)
    } else {
      if (graph.tails.size > 1) {
        throw new RuntimeException("sorry we are not able to determine the end of the workflow, please utilize manual linking!")
      }

      val tail = graph.tails.head
      graph.addNode(node)
      graph.addEdge(Edge(tail.id, node.id))
    }

    this
  }


  /**
    * adds a new step, based on the provided processor. Please be aware that this must have the step annotation
    * and will do the linking, etc based on provided information in the annotation or add it to the last element
    * before the given step
    *
    * @param processor
    * @return
    */

  def add(processor: ItemProcessor[TYPE, TYPE], connectedTo: String = null): WorkflowBuilder[TYPE] = {
    val connectivity = helper.buildNodeAndEdge(processor, processor.getClass.getAnnotation(classOf[Step]), graph)

    if (connectedTo == null) {
      if (annotationLinking) {
        connectivity._2 match {
          case Some(x) =>
            graph.addNode(connectivity._1)
            graph.addEdge(x)
          case None =>
            graph.addNode(connectivity._1)
        }
      } else {
        add(connectivity._1.step)
      }
    } else {
      graph.addNode(connectivity._1)
      graph.addEdge(Edge(connectedTo, connectivity._1.id))
    }
    this
  }

  /**
    * this adds all the defined processors in the list
    * and optional allows linear linking (default) or linking based by annotations. It will internally
    * always use the provided annotation values to generate id's and descriptions
    *
    * @param processors
    * @return
    */
  def add(processors: Iterable[ItemProcessor[TYPE, TYPE]]): WorkflowBuilder[TYPE] = {
    if (annotationLinking) {
      processors.foreach { processor =>
        add(processor)
      }
    } else {
      processors.foreach { processor =>
        val processingStep: ProcessingStep[TYPE, TYPE] = helper.generateProcessingStepFromAnnotation(processor, processor.getClass.getAnnotation(classOf[Step]), graph)
        add(processingStep)
      }
    }

    this
  }


  /**
    * adds the given step and instructs the graph to connect it to the specified step. The user has to keep track of connections in this way!
    *
    * @param step
    * @param connectedTo
    * @return
    */
  def add(step: ProcessingStep[TYPE, TYPE], connectedTo: ProcessingStep[TYPE, TYPE]): WorkflowBuilder[TYPE] = {
    val node = Node[TYPE, TYPE](step.name, step, step.description)
    graph.addNode(node)
    graph.addEdge(Edge(node.id, connectedTo.name))

    this
  }

  /**
    * should the workflow be openended or linear only
    *
    * @param linear
    */
  def forceLinear(linear: Boolean = true): WorkflowBuilder[TYPE] = {
    linearOnly = linear
    this
  }

  /**
    * enables the annotations to be used for linking
    *
    * @param enable
    */
  def enableAnnotationLinking(enable: Boolean = true): WorkflowBuilder[TYPE] = {
    annotationLinking = enable
    this
  }

  /**
    * registers a listener with the workflow for custom notifications
    *
    * @param listener
    */
  def add(listener: WorkflowListener[TYPE]): WorkflowBuilder[TYPE] = {
    listeners.add(listener)
    this
  }

  /**
    * overloaded add operator
    *
    * @param listener
    * @return
    */
  def +(listener: WorkflowListener[TYPE]): WorkflowBuilder[TYPE] = {
    add(listener)
  }

  /**
    * builds the graphs based on all the provided arguments
    *
    * @return
    */
  def build(): Workflow[TYPE] = {
    if (alreadyBuild) {
      throw new RuntimeException("we are sorry, you need to create a new builder, this one is exhausted!")
    } else {
      alreadyBuild = true
      val workflow = new Workflow[TYPE](graph, !linearOnly)

      listeners.asScala.foreach(workflow.addListener)
      workflow
    }
  }
}

/**
  * This is a simple helper class to work with annotations
  * and allows us to easily build edges
  */
class AnnotationHelper[TYPE] extends LazyLogging {

  def generateProcessingStepFromAnnotation(itemProcessor: ItemProcessor[TYPE, TYPE], step: Step, graph: Graph[String, Node[TYPE, TYPE], Edge]): ProcessingStep[TYPE, TYPE] = {
    val name = generateNodeIdentifier(step, itemProcessor, parentScan = false, graph)

    //build the new processing step
    ProcessingStep(name, itemProcessor, step.description())
  }

  /**
    * finds the edge linkage for the given step for us
    *
    * @param step
    * @return
    */
  def buildNodeAndEdge(itemProcessor: ItemProcessor[TYPE, TYPE], step: Step, graph: Graph[String, Node[TYPE, TYPE], Edge]): (Node[TYPE, TYPE], Option[Edge]) = {

    val processingStep = generateProcessingStepFromAnnotation(itemProcessor, step, graph)
    val parent = getPreviousStepId(step, graph)

    //create a new node
    val node = Node[TYPE, TYPE](processingStep.name, processingStep, step.description())

    logger.info(s"previous step name: $parent")

    if (parent == "None") {
      logger.info("this is the root node")
      (node, None)
    } else {
      logger.info(s"step is mapped from $parent to ${processingStep.name}")
      (node, Option(Edge(parent, processingStep.name)))
    }
  }


  /**
    * attempts to find the id for the previous step defined, depending if it's done as class or as parameter
    *
    * @param step
    * @return
    */
  def getPreviousStepId(step: Step, graph: Graph[String, Node[TYPE, TYPE], Edge]): String = {
    //the parent class is not void
    if (step.previousClass().getName != classOf[Void].getName) {
      logger.info(s"custom class name specified for previous step: ${step.previousClass()}")
      if (step.previous() != "None") {
        throw new ParentAndParentClassSpecifiedException
      } else {
        logger.info(s"looking up referenced bean: ${step.previousClass()}")
        val parentBean = step.previousClass()
        val referenceId = parentBean.getAnnotation(classOf[Step])

        if (referenceId == null) {
          throw new RefrenceBeanHasNotBeenAnnotatedException(s"need @Step annotation $parentBean")
        } else {
          logger.info("generating identifier information for the previous step")
          generateNodeIdentifier(referenceId, parentBean, parentScan = true, graph)
        }
      }
    } else {
      logger.info(s"using defined name for the previous step of: ${step.previous()}")
      step.previous()
    }
  }

  /**
    * attempts to generate the name for the node processor
    *
    * @param step
    * @param processor
    * @return
    */
  def generateNodeIdentifier(step: Step, processor: Any, parentScan: Boolean, graph: Graph[String, Node[TYPE, TYPE], Edge]): String = {
    var name = step.name()
    //assigning class name as default
    if (name == "None") name = processor.getClass.getName

    //check if this id was already registered
    graph.getNode(name) match {
      case None =>
        logger.debug(s"node name is unique $name")
        name
      case _ =>
        if (!parentScan)
          throw new NameAlreadyRegisteredException(s"a node with the name '$name' was already registered, please use unique names")
        else
          name
    }
  }
}

/**
  * creates a new workflow builder for us
  */
object WorkflowBuilder {

  def create[TYPE: ClassTag]: WorkflowBuilder[TYPE] = new WorkflowBuilder[TYPE]
}