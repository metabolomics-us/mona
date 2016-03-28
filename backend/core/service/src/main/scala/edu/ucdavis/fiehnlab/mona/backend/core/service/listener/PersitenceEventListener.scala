package edu.ucdavis.fiehnlab.mona.backend.core.service.listener

/**
  * this listener is used to inform subscribers about changes to the mona database system. They should be annotated as @Component, which will automatically add them to the related classes
  */
trait PersitenceEventListener [T]{

  /**
    * an entry was added to the system
    * @param event
    */
  def added(event:PersistenceEvent[T])

  /**
    * the event was updated in the system
    * @param event
    */
  def updated(event:PersistenceEvent[T])

  /**
    * an entry was deleted from the system
    * @param event
    */
  def deleted(event:PersistenceEvent[T])

  /**
    * the priority of the listener
    * @return
    */
  def priority:Int = 0
}

/**
  * a simple event, that something in the persistence layer was changed
  * @param content
  * @tparam T
  * @param firedAt when the event occured
  */
case class PersistenceEvent[T](content:T, firedAt:java.util.Date)