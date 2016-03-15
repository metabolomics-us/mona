package edu.ucdavis.fiehnlab.mona.backend.core.workflow.exception

import scala.collection.script.Message

/**
  * Created by wohlgemuth on 3/14/16.
  */
class WorkflowException(message:String) extends Exception(message){

}

class NameAlreadyRegisteredException(message: String) extends WorkflowException(message)


