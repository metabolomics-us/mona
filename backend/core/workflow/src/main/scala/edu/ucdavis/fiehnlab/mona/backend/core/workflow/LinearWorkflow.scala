package edu.ucdavis.fiehnlab.mona.backend.core.workflow

import edu.ucdavis.fiehnlab.mona.backend.core.workflow.exception.WorkflowDoesntSupportMoreThanOneChieldExcpetion
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph.Node
import org.springframework.batch.item.ItemProcessor

import scala.reflect.ClassTag

/**
  * a simple linear workflow
  */
class LinearWorkflow[TYPE:ClassTag](name:String) extends Workflow[TYPE](name) with ItemProcessor[TYPE, TYPE]{

  override def process(toProcess: TYPE, node: Node[TYPE, TYPE] = graph.heads.head): TYPE = {

    val step = node.step

    fireStartingEvent(toProcess, step)

    logger.debug(s"executing workflow step ${step.name}, ${step.description}")
    val result: TYPE = step.processor.process(toProcess)

    fireFinishingEvent(result, step)

    val children = graph.getChildren(node)

    if (children.isEmpty) {
      result
    }
    else if(children.size == 1){
      process(result,children.head)
    }
    else {
      throw new WorkflowDoesntSupportMoreThanOneChieldExcpetion(s"defined workflow had several children: ${children}" )
    }
  }

  /**
    * simple wrapper around the internal api
 *
    * @param item
    * @return
    */
  override def process(item: TYPE): TYPE = process(item,graph.heads.head)
}

/**
  *
  * a graph based workflow, which can return several results
  *
  * @param name
  * @tparam TYPE
  */
class GraphWorkflow[TYPE:ClassTag](name:String) extends Workflow[TYPE](name) with ItemProcessor[TYPE,List[TYPE]]{

  override def process(toProcess: TYPE, node: Node[TYPE, TYPE] = graph.heads.head): List[TYPE] = {

    val step = node.step

    fireStartingEvent(toProcess, step)

    logger.debug(s"executing workflow step ${step.name}, ${step.description}")
    val result: TYPE = step.processor.process(toProcess)

    fireFinishingEvent(result, step)

    val children = graph.getChildren(node)

    if (children.isEmpty) {
      result :: List.empty
    }
    else {
      children.collect {
        case child: Node[TYPE, TYPE] => process(result, child)
      }.flatten.toList
    }
  }

  /**
    * simple wrapper
    * @param item
    * @return
    */
  override def process(item: TYPE): List[TYPE] = process(item)
}
