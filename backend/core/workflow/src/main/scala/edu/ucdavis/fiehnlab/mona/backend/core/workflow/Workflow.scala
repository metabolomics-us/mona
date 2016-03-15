package edu.ucdavis.fiehnlab.mona.backend.core.workflow

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.exception.NameAlreadyRegisteredException
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph._
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component


/**
  * defines a standard processing workflow
  */
@Component
class Workflow extends BeanPostProcessor with LazyLogging {

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
  def run(spectrum: Spectrum) : Unit = {
    run(spectrum,graph.head)
  }

  /**
    * iterative approach to process all defined annotation data
    * @param spectrum
    * @param node
    */
  protected def run(spectrum:Spectrum, node:Node ) : Unit= {

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
    * does nothing in this instance
    *
    * @param bean
    * @param name
    * @return
    */
  override def postProcessAfterInitialization(bean: scala.AnyRef, name: String): AnyRef = {
    scanBeanForAnnotations(bean, name)
    bean
  }

  override def postProcessBeforeInitialization(bean: scala.AnyRef, name: String): AnyRef = bean

  /**
    * attempts to find our required annotations
    * and builds the internal graph based on this
    *
    * @param bean
    */
  def scanBeanForAnnotations(bean: AnyRef, name: String): Unit = {
    logger.info(s"searching for annotations: ${bean}")
    bean match {

      case processor: ItemProcessor[Spectrum, Spectrum] =>

        logger.info("found processor...")
        val step: Step = bean.getClass.getAnnotation(classOf[Step])

        if (step != null) {
          logger.info("\t => with the correct annotation")

          var name = step.name()
          var parent = step.previous()
          var parentClass = step.previousClass()

          //build the new processing step
          val proccessingStep = ProcessingStep(name, processor,step.description())

          //assigning class name as default
          if (name == "None") {
            name = processor.getClass.getName
          }

          //check if this id was already registered
          graph.getNode(name) match {
            case None =>
              logger.debug(s"node name is unique ${name}")
            case _ =>
              throw new NameAlreadyRegisteredException(s"a node with the name '${name}' was already registered, please use unique names")
          }

          //create a new node
          val node = Node(name,proccessingStep,step.description())
          graph.addNode(node)

          //is a previous registered already
          graph.getNode(parent) match {
            case None =>
              logger.debug(s"node ${name} will become root element")
            case Some(x) =>
              graph.addEdge(Edge(node.id,x.id))
          }

        }
      case _ =>
        logger.debug(s"unsupported bean found: ${bean.getClass}")
    }
  }
}
