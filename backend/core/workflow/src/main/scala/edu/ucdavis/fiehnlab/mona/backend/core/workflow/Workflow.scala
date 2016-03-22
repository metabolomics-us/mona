package edu.ucdavis.fiehnlab.mona.backend.core.workflow

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.exception.{NameAlreadyRegisteredException, ParentAndParentClassSpecifiedException, RefrenceBeanHasNotBeenAnnotatedException, WorkflowException}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph._
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.listener.WorkflowListener
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.{ApplicationContext, ApplicationListener}

import scala.collection.JavaConverters._
import scala.reflect._


/**
  * defines a standard processing workflow
  */
abstract class Workflow[TYPE: ClassTag](val name: String) extends ApplicationListener[ContextRefreshedEvent] with LazyLogging {

  @Autowired
  val applicationContext: ApplicationContext = null

  /**
    * internal graph for our workflow
    */
  val graph = new Graph[String, Node[TYPE, TYPE], Edge]

  /**
    * associated listener
    */
  @Autowired(required = false)
  val listeners: java.util.List[WorkflowListener[TYPE]] = null

  /**
    * iterative approach to process all defined annotation data
    *
    * @param toProcess
    * @param node by default it's the head node
    */
  def process(toProcess: TYPE, node: Node[TYPE, TYPE] = graph.heads.head): Any

  /**
    * should not longer be utilized, instead the 'process' method should be used
    * @param toProcess
    * @param node
    * @return
    */
  @Deprecated
  final def run(toProcess: TYPE, node: Node[TYPE, TYPE] = graph.heads.head): Any = {
    logger.warn("using deprecated method, this will go away soon...")
    process(toProcess,node)
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

  /**
    * mow many steps do we need to execute
    *
    * @return
    */
  def stepSize = graph.size

  /**
    * attempts to find our required annotations
    * and builds the internal graph based on this
    *
    * @param bean
    */
  def scanBeanForAnnotations(bean: Any, name: String): Unit = {
    logger.info(s"searching for annotations: ${bean}")
    bean match {

      case processor: ItemProcessor[TYPE, TYPE] =>


        logger.info("found processor...")
        val step: Step = bean.getClass.getAnnotation(classOf[Step])

        if (step.workflow() == this.name) {
          logger.info("\t => with the correct annotation")

          val name = generateNodeIdentifier(step, processor)

          //build the new processing step
          val proccessingStep = ProcessingStep(name, processor, step.description())

          //create a new node
          val node = Node[TYPE, TYPE](name, proccessingStep, step.description())
          graph.addNode(node)

          val parent = getPreviousStepId(step)

          logger.info(s"previous step name: ${parent}")

          if (parent == "None") {
            logger.info("this is the root node")
          }
          else {
            logger.info(s"step is mapped from ${parent} to ${name}")
            graph.addEdge(new Edge(parent, name))
          }
        }
        else {
          logger.debug(s"skipping step $step since it belongs to a different workflow")
        }
      case _ =>
        logger.debug(s"unsupported bean found: ${bean.getClass}")
    }
  }

  /**
    * attempts to find the id for the previous step defined, depending if it's done as class or as parameter
    *
    * @param step
    * @return
    */
  def getPreviousStepId(step: Step): String = {
    //the parent class is not void
    if (step.previousClass().getName != classOf[Void].getName) {
      logger.info(s"custom class name specified for previous step: ${step.previousClass()}")
      if (step.previous() != "None") {
        throw new ParentAndParentClassSpecifiedException
      }
      else {
        logger.info(s"looking up referenced bean: ${step.previousClass()}")
        val parentBean = applicationContext.getBean(step.previousClass())

        logger.info(s"found: ${parentBean}")

        val referenceId = parentBean.getClass.getAnnotation(classOf[Step])

        if (referenceId == null) {
          throw new RefrenceBeanHasNotBeenAnnotatedException(s"need @Step annotation ${parentBean}")
        }
        else {
          logger.info("generating identifier information for the previous step")
          generateNodeIdentifier(referenceId, parentBean, true)
        }
      }
    }
    else {
      logger.info(s"using defined name for the previous step of: ${step.previous()}")
      step.previous()
    }
  }

  /**
    * attemps to generate the name for the node processor
    *
    * @param step
    * @param processor
    * @return
    */
  def generateNodeIdentifier(step: Step, processor: Any, parentScan: Boolean = false): String = {
    var name = step.name()
    //assigning class name as default
    if (name == "None") name = processor.getClass.getName

    //check if this id was already registered
    graph.getNode(name) match {
      case None =>
        logger.debug(s"node name is unique ${name}")
        name
      case _ =>
        if (!parentScan)
          throw new NameAlreadyRegisteredException(s"a node with the name '${name}' was already registered, please use unique names")
        else
          name
    }
  }

  /**
    * searches for all our annoations
    *
    * @param event
    */
  override def onApplicationEvent(event: ContextRefreshedEvent): Unit = {
    val steps: util.Map[String, AnyRef] = applicationContext.getBeansWithAnnotation(classOf[Step])

    logger.debug(s"found ${steps} steps to process")
    steps.asScala.keys.foreach { key =>
      logger.debug(s"scanning properties for ${key}")
      scanBeanForAnnotations(steps.get(key), key)
    }


    if (graph.heads.isEmpty) {
      throw new WorkflowException(s"you need to annotate at least 1 workflow step with @Step and ensure it's of the type ${classTag[TYPE].runtimeClass}")
    }
    else if (graph.heads.size != 1) {
      throw new WorkflowException(s"the defined workflow results in more than 1 beginning, please ensure you define a directional graph. Heads were ${graph.heads}")
    }
    logger.debug("all steps are processed")
  }
}

/**
  * a conditional action, which can return true or false. Implmented classes need to be annotated with the Condition annotation to define the behavior
  * @tparam T
  */
trait Conditional[T] {

  /**
    * evaluate the object value and return a value depending on the evaluation
    * @param value
    * @return
    */
  def evaluate(value:T) : Boolean
}