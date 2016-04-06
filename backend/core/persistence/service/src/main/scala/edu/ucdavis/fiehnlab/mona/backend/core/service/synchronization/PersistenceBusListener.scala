package edu.ucdavis.fiehnlab.mona.backend.core.service.synchronization

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, EventBusListener}
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.service.listener.PersitenceEventListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

/**
  * forwards events to the event bus listener
  */
@Service
class PersistenceBusListener extends PersitenceEventListener[Spectrum]{

  @Autowired
  private val eventBus:EventBus[Spectrum] = null

  /**
    * an entry was added to the system
    *
    * @param event
    */
  override def added(event: Event[Spectrum]): Unit = eventBus.sendEvent(event)

  /**
    * the event was updated in the system
    *
    * @param event
    */
  override def updated(event: Event[Spectrum]): Unit = eventBus.sendEvent(event)

  /**
    * an entry was deleted from the system
    *
    * @param event
    */
  override def deleted(event: Event[Spectrum]): Unit = eventBus.sendEvent(event)

  /**
    * the priority of the listener
    *
    * @return
    */
  override def priority: Int = -100
}
