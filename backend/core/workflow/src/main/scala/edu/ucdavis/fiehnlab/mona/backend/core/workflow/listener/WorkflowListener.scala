package edu.ucdavis.fiehnlab.mona.backend.core.workflow.listener

import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step

/**
  * Created by wohlgemuth on 3/15/16.
  */
trait WorkflowListener[T] {

  /**
    * started processing this step
    * @param value
    * @param step
    */
  def startedProcessing(value:T, step:Step)

  /**
    * finished processing with this step
    * @param value
    * @param step
    */
  def finishedProcessing(value:T, step:Step)
}
