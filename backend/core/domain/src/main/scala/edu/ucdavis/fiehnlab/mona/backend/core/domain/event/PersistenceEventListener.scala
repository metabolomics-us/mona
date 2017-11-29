package edu.ucdavis.fiehnlab.mona.backend.core.domain.event

import com.typesafe.scalalogging.LazyLogging

/**
  * this listener is used to inform subscribers about changes to the mona database system. They should be annotated as @Component, which will automatically add them to the related classes
  */
trait PersistenceEventListener[T] extends LazyLogging {

  /**
    * an entry was added to the system
    *
    * @param event
    */
  def added(event: Event[T]): Unit

  /**
    * the event was updated in the system
    *
    * @param event
    */
  def updated(event: Event[T]): Unit

  /**
    * an entry was deleted from the system
    *
    * @param event
    */
  def deleted(event: Event[T]): Unit

  /**
    * requesting a synchronization
    *
    * @param event
    */
  def sync(event: Event[T]): Unit

  /**
    * reacts to an event
    *
    * @param event
    */
  def handleEvent(event: Event[T]): Unit = event.eventType.toLowerCase match {
    case Event.UPDATE => updated(event)
    case Event.DELETE => deleted(event)
    case Event.ADD => added(event)
    case Event.SYNC => sync(event)
    case _ => logger.warn(s"invalid event processed: ${event.eventType}")
  }

  /**
    * the priority of the listener
    *
    * @return
    */
  def priority: Int = 0
}
