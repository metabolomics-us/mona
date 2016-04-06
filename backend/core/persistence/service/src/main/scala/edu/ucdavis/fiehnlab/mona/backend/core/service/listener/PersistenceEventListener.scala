package edu.ucdavis.fiehnlab.mona.backend.core.service.listener

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events.{AddEvent, DeleteEvent, Event, UpdateEvent}

/**
  * this listener is used to inform subscribers about changes to the mona database system. They should be annotated as @Component, which will automatically add them to the related classes
  */
trait PersitenceEventListener[T] {

  /**
    * an entry was added to the system
    *
    * @param event
    */
  def added(event: Event[T])

  /**
    * the event was updated in the system
    *
    * @param event
    */
  def updated(event: Event[T])

  /**
    * an entry was deleted from the system
    *
    * @param event
    */
  def deleted(event: Event[T])

  /**
    * reacts to an event
    *
    * @param event
    */
  def handleEvent(event: Event[T]) = event match {
    case x: UpdateEvent[T] => updated(x)
    case x: DeleteEvent[T] => deleted(x)
    case x: AddEvent[T] => added(x)
    case _ =>
  }

  /**
    * the priority of the listener
    *
    * @return
    */
  def priority: Int = 0
}
