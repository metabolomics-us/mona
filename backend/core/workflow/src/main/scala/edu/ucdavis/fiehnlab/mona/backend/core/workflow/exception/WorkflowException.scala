package edu.ucdavis.fiehnlab.mona.backend.core.workflow.exception

/**
  * Created by wohlgemuth on 3/14/16.
  */
class WorkflowException(message: String) extends Exception(message)

class WorkflowDoesntSupportMoreThanOneChieldExcpetion(message: String) extends WorkflowException(message)

class NameAlreadyRegisteredException(message: String) extends WorkflowException(message)

class ReferencedClassNotFound(message: String) extends WorkflowException(message)

class ParentAndParentClassSpecifiedException() extends WorkflowException("please only specify a parent or a parent class, but not both")

class RefrenceBeanHasNotBeenAnnotatedException(message: String) extends WorkflowException(message)