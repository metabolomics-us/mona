package edu.ucdavis.fiehnlab.mona.backend.core.workflow

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.exception._
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph._
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.listener.WorkflowListener
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.{ApplicationContext, ApplicationListener}

import scala.collection.JavaConverters._
import scala.reflect._


/**
  * defines an workflow
  *
  * @param graph
  * @tparam TYPE
  */
class Workflow[TYPE: ClassTag](val graph: Graph[String, Node[TYPE, TYPE], Edge] = new Graph[String, Node[TYPE, TYPE], Edge], val permitsTree: Boolean = false) extends ItemProcessor[TYPE, List[TYPE]] with LazyLogging {

  /**
    * how many steps do we need to execute
    *
    * @return
    */
  def stepSize: Int = graph.size

  /**
    * associated listener
    */
  @Autowired(required = false)
  val listeners: java.util.List[WorkflowListener[TYPE]] = new java.util.ArrayList[WorkflowListener[TYPE]]

  /**
    * registers an additional listener
    *
    * @param listener
    * @return
    */
  def addListener(listener: WorkflowListener[TYPE]): Boolean = listeners.add(listener)

  /**
    * processes the given item for us
    *
    * @param item
    * @return
    */
  def process(item: TYPE): List[TYPE] = {
    if(graph.size > 0) {
      process(item, graph.heads.head)
    } else{
      throw new RuntimeException("please provide at least 1 task to be executed!")
    }
  }

  /**
    * iterative approach to process all defined annotation data
    *
    * @param toProcess
    * @param node by default it's the head node
    */
  protected final def process(toProcess: TYPE, node: Node[TYPE, TYPE]): List[TYPE] = {

    val step = node.step

    fireStartingEvent(toProcess, step)

    logger.debug(s"executing workflow step ${step.name}, ${step.description}")
    val result: TYPE = step.processor.process(toProcess)

    fireFinishingEvent(result, step)

    val children = graph.getChildren(node)

    //if we got no elements, we just return a list with our data
    if (children.isEmpty) {
      result :: List.empty
    }

    //if we got 1 element, we return the processed result
    else if (children.size == 1) {

      process(result, children.head)
    }

    //we got a graph structure
    else {
      //we allow trees
      if (permitsTree) {
        //process every child in the tree
        children.collect {
          case child: Node[TYPE, TYPE] => process(result, child)
        }.flatten.toList
      } else {
        throw new WorkflowDoesntSupportMoreThanOneChieldExcpetion(s"defined workflow had several children: $children")
      }
    }
  }

  /**
    * fires a finishing event
    *
    * @param toProcess
    * @param step
    */
  def fireFinishingEvent(toProcess: TYPE, step: ProcessingStep[TYPE, TYPE]): Unit = {
    if (listeners != null) {
      listeners.asScala.foreach(_.finishedProcessing(toProcess, step.processor.getClass.getAnnotation(classOf[Step])))
    }
  }

  /**
    * fires an event every time a process starts
    *
    * @param toProcess
    * @param step
    */
  def fireStartingEvent(toProcess: TYPE, step: ProcessingStep[TYPE, TYPE]): Unit = {
    if (listeners != null) {
      listeners.asScala.foreach(_.startedProcessing(toProcess, step.processor.getClass.getAnnotation(classOf[Step])))
    }
  }
}

/**
  * defines a standard processing workflow
  */
class AnnotationWorkflow[TYPE: ClassTag](val name: String, permitsTree: Boolean = false) extends Workflow(new Graph[String, Node[TYPE, TYPE], Edge], permitsTree) with ApplicationListener[ContextRefreshedEvent] {

  @Autowired
  val applicationContext: ApplicationContext = null

  val helper: AnnotationHelper[TYPE] = new AnnotationHelper[TYPE]()

  /**
    * attempts to find our required annotations
    * and builds the internal graph based on this
    *
    * @param bean
    */
  def scanBeanForAnnotations(bean: Any, name: String): Unit = {
    logger.info(s"searching for annotations: $bean")

    bean match {
      case processor: ItemProcessor[TYPE, TYPE] =>
        logger.info("found processor...")

        val step: Step = bean.getClass.getAnnotation(classOf[Step])

        if (step.workflow() == this.name) {
          logger.info("\t => with the correct annotation")

          val connectivity = helper.buildNodeAndEdge(processor, step, graph)

          connectivity._2 match {
            case Some(x) =>
              graph.addNode(connectivity._1)
              graph.addEdge(x)
            case None =>
              graph.addNode(connectivity._1)
              logger.info("this is the root node")
          }
        } else {
          logger.debug(s"skipping step $step since it belongs to a different workflow")
        }
      case _ =>
        logger.debug(s"unsupported bean found: ${bean.getClass}")
    }
  }


  /**
    * searches for all our annoations
    *
    * @param event
    */
  override def onApplicationEvent(event: ContextRefreshedEvent): Unit = {
    val steps: util.Map[String, AnyRef] = applicationContext.getBeansWithAnnotation(classOf[Step])

    logger.debug(s"found $steps steps to process")
    steps.asScala.keys.foreach { key =>
      logger.debug(s"scanning properties for $key")
      scanBeanForAnnotations(steps.get(key), key)
    }

    if (graph.heads.isEmpty) {
      throw new WorkflowException(s"you need to annotate at least 1 workflow step with @Step and ensure it's of the type ${classTag[TYPE].runtimeClass}")
    } else if (graph.heads.size != 1) {
      throw new WorkflowException(s"the defined workflow results in more than 1 beginning, please ensure you define a directional graph. Heads were ${graph.heads}")
    }

    logger.debug("all steps are processed")
  }
}

/**
  * a conditional action, which can return true or false. Implmented classes need to be annotated with the Condition annotation to define the behavior
  *
  * @tparam T
  */
trait Conditional[T] {

  /**
    * evaluate the object value and return a value depending on the evaluation
    *
    * @param value
    * @return
    */
  def evaluate(value: T): Boolean
}