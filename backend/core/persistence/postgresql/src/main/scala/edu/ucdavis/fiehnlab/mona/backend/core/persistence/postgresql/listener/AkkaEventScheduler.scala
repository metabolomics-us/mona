package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.listener

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinPool
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.{Event, PersistenceEventListener, EventScheduler}

import scala.jdk.CollectionConverters._

/**
 * Akka based event scheduler to send events using an actor and so allows for higher throughput
 * of events compared to the default implementation
 *
 * @tparam T
 */
class AkkaEventScheduler[T] extends EventScheduler[T] {

  val system = ActorSystem("MonaEventScheduler")

  // A fixed pool of workers instead of an actor per event. Actors are never garbage collected
  // until explicitly stopped, so spawning one per event permanently leaked an actor for every
  // persistence event and slowly exhausted the heap
  val workers: ActorRef = system.actorOf(RoundRobinPool(5).props(Props[SchedulingActor[T]]))

  /**
   * Schedules the processing of the given event to be processed down stream
   *
   * @param event
   */
  override def scheduleEventProcessing(event: Event[T]): Unit = {
    workers ! (persistenceEventListeners, event)
  }
}

/**
 * The actual actor which receives a copy of all our data
 *
 * @tparam T
 */
class SchedulingActor[T] extends Actor with LazyLogging {

  /**
   * When we receive our data. Rather ugly and would be nicer to inject this somehow with spring instead of doing this
   *
   * @return
   */
  override def receive: Receive = {
    case x: (java.util.List[PersistenceEventListener[T]], Event[T]) =>
      logger.debug("\nnotify listeners")

      x._1.asScala.sortBy(_.priority).reverse.foreach { listener =>
        logger.debug(s"Calling ${listener.getClass.getSimpleName} with event ${x._2.eventType}")
        listener.handleEvent(x._2)
      }
  }
}
