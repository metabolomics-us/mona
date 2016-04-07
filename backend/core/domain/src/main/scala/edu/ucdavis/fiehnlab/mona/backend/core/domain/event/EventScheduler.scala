package edu.ucdavis.fiehnlab.mona.backend.core.domain.event

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
  val persistenceEventListeners: java.util.List[PersistenceEventListener[T]] = null

  /**
    * schedules the processing of the given event to be processed down stream
    *
    * @param event
    */
  def scheduleEventProcessing(event: Event[T]) = persistenceEventListeners.asScala.sortBy(_.priority).reverse.foreach(_.handleEvent(event))
}

