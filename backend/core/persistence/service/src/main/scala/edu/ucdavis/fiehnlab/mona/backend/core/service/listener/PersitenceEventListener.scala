package edu.ucdavis.fiehnlab.mona.backend.core.service.listener

/**
  * this listener is used to inform subscribers about changes to the mona database system. They should be annotated as @Component, which will automatically add them to the related classes
  */
trait PersitenceEventListener[T] {

  /**
    * an entry was added to the system
    *
    * @param event
    */
  def added(event: PersistenceEvent[T])

  /**
    * the event was updated in the system
    *
    * @param event
    */
  def updated(event: PersistenceEvent[T])

  /**
    * an entry was deleted from the system
    *
    * @param event
    */
  def deleted(event: PersistenceEvent[T])

  /**
    * reacts to an event
    *
    * @param event
    */
  def handleEvent(event: PersistenceEvent[T]) = event match {
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

/**
  * a simple event, that something in the persistence layer was changed
  *
  * @param content
  * @tparam T
  * @param firedAt when the event occured
  */
class PersistenceEvent[T](val content: T, val firedAt: java.util.Date)

case class UpdateEvent[T](override val content: T, override val firedAt: java.util.Date) extends PersistenceEvent[T](content, firedAt)

case class AddEvent[T](override val content: T, override val firedAt: java.util.Date) extends PersistenceEvent[T](content, firedAt)

case class DeleteEvent[T](override val content: T, override val firedAt: java.util.Date) extends PersistenceEvent[T](content, firedAt)