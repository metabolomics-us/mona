package edu.ucdavis.fiehnlab.mona.backend.core.service.listener

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem, Props}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

/**
  * handles the execution and processing of event for our services. This is the simples possible implementation
  * and should be replaced with an Actor or Message drives one for production use
  */
@Component
class EventScheduler[T] {

  /**
    * contains all listeneres in the system to tell subscripers that something with the backend happend
    */
  @Autowired(required = false)
  val persistenceEventListeners: java.util.List[PersitenceEventListener[T]] = null

  /**
    * schedules the processing of the given event to be processed down stream
    *
    * @param event
    */
  def scheduleEventProcessing(event: PersistenceEvent[T]) = persistenceEventListeners.asScala.sortBy(_.priority).reverse.foreach(_.handleEvent(event))
}

/**
  * akka based event scheduler to send events using an actor and so allows for higher throughput
  * of events
  *
  * @tparam T
  */
class AkkaEventScheduler[T] extends EventScheduler[T] {

  val system = ActorSystem("MonaEventScheduler")

  /**
    * schedules the processing of the given event to be processed down stream
    *
    * @param event
    */
  override def scheduleEventProcessing(event: PersistenceEvent[T]): Unit = {
    val actor = system.actorOf(Props[SchedulingActor[T]])
    actor ! (persistenceEventListeners, event)
  }
}

/**
  * the actual actor, which receives a copy of all our
  *
  * @tparam T
  */
class SchedulingActor[T] extends Actor {

  /**
    * when we receive our data. Rather uggly and would be nicer to inject this somehow with spring instead of doing this
    *
    * @return
    */
  override def receive: Receive = {
    case x: (java.util.List[PersitenceEventListener[T]], PersistenceEvent[T]) =>
      x._1.asScala.sortBy(_.priority).reverse.foreach(_.handleEvent(x._2))
  }
}