package edu.ucdavis.fiehnlab.mona.backend.core.workflow

import java.util

import scala.collection.JavaConverters._
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.exception.{WorkflowException, RefrenceBeanHasNotBeenAnnotatedException, ParentAndParentClassSpecifiedException, NameAlreadyRegisteredException}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph._
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.{ApplicationListener, ApplicationContext}
import org.springframework.stereotype.Component


/**
  * defines a standard processing workflow
  */
@Component
class Workflow extends ApplicationListener[ContextRefreshedEvent] with LazyLogging {

  @Autowired
  val applicationContext: ApplicationContext = null

  /**
    * internal graph for our workflow
    */
  val graph: Graph[String, Node, Edge] = new Graph[String, Node, Edge]

  /**
    * executes the workflow for the given spectrum
    *
    * @param spectrum
    * @return
    */
  def run(spectrum: Spectrum): Unit = {
    run(spectrum, graph.heads.head)
  }

  /**
    * iterative approach to process all defined annotation data
    *
    * @param spectrum
    * @param node
    */
  protected def run(spectrum: Spectrum, node: Node): Unit = {

    logger.debug(s"working on spectrum with id ${spectrum.id}")
    val step = node.step

    logger.debug(s"executing workflow step ${step.name}, ${step.description}")
    val result = step.processor.process(spectrum)

    graph.getChildren(node).foreach { child: Node =>
      run(result, child)
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

      case processor: ItemProcessor[Spectrum, Spectrum] =>

        logger.info("found processor...")
        val step: Step = bean.getClass.getAnnotation(classOf[Step])

        logger.info("\t => with the correct annotation")

        val name = generateNodeIdentifier(step, processor)

        //build the new processing step
        val proccessingStep = ProcessingStep(name, processor, step.description())

        //create a new node
        val node = Node(name, proccessingStep, step.description())
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
          generateNodeIdentifier(referenceId, parentBean)
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
  def generateNodeIdentifier(step: Step, processor: Any): String = {
    var name = step.name()
    //assigning class name as default
    if (name == "None") name = processor.getClass.getName

    //check if this id was already registered
    graph.getNode(name) match {
      case None =>
        logger.debug(s"node name is unique ${name}")
        name
      case _ =>
        throw new NameAlreadyRegisteredException(s"a node with the name '${name}' was already registered, please use unique names")
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


    if (graph.heads.size != 1) {
      throw new WorkflowException(s"the defined workflow results in more than 1 beginning, please ensure you define a directional graph. Heads were ${graph.heads}")
    }
    logger.debug("all steps are processed")
  }
}
