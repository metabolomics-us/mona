package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events.{AddEvent, DeleteEvent, Event, UpdateEvent}
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

/**
  * any instance of this class automatically receive events and should ensure that
  * they utilize is to react to them
  */
@Service
abstract class EventBusListener[T] extends LazyLogging{

  /**
    * an element has been received from the bus and should be now processed
 *
    * @param event
    */
  @RabbitListener(queues = Array("mona-event-bus"))
  final def received(event:Event[T]) : Unit = {
    event match {
      case x: UpdateEvent[T] => updated(x)
      case x: DeleteEvent[T] => deleted(x)
      case x: AddEvent[T] => added(x)
      case _ =>
        logger.debug(s"none supported event found ${event}")
    }
  }

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

}
