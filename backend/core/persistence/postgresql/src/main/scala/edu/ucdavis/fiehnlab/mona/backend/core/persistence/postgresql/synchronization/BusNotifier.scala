package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.synchronization

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.{Event, PersistenceEventListener}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * forwards events to the event bus listener
 */
@Service
class BusNotifier extends PersistenceEventListener[SpectrumResult] {

  @Autowired
  private val eventBus: EventBus[SpectrumResult] = null

  /**
   * an entry was added to the system
   *
   * @param event
   */
  override def added(event: Event[SpectrumResult]): Unit = {
    logger.debug(s"forwarding ${event.eventType} event to bus")
    eventBus.sendEvent(event)
  }

  /**
   * the event was updated in the system
   *
   * @param event
   */
  override def updated(event: Event[SpectrumResult]): Unit = {
    logger.debug(s"forwarding ${event.eventType} event to bus")
    eventBus.sendEvent(event)
  }

  /**
   * an entry was deleted from the system
   *
   * @param event
   */
  override def deleted(event: Event[SpectrumResult]): Unit = {
    logger.debug(s"forwarding ${event.eventType} event to bus")
    eventBus.sendEvent(event)
  }

  /**
   * the priority of the listener
   *
   * @return
   */
  override def priority: Int = -100
}
