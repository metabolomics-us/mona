package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.synchronization

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.{Event, PersistenceEventListener}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Profile

/**
 * Created by wohlgemuth on 3/17/16.
 */
@Component
@Profile(Array("mona.persistence"))
class CountListener extends PersistenceEventListener[Spectrum] with LazyLogging {
  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  /**
   * an entry was added to the system
   *
   * @param event
   */
  override def added(event: Event[Spectrum]): Unit = {
    logger.debug(s"added spectrum count is now ${spectrumPersistenceService.count()}")
  }

  /**
   * the event was updated in the system
   *
   * @param event
   */
  override def updated(event: Event[Spectrum]): Unit = {
    logger.debug(s"updated spectrum count is now ${spectrumPersistenceService.count()}")
  }

  /**
   * an entry was deleted from the system
   *
   * @param event
   */
  override def deleted(event: Event[Spectrum]): Unit = {
    logger.debug(s"deleted spectrum count is now ${spectrumPersistenceService.count()}")
  }

  /**
   * the priority of the listener
   *
   * @return
   */
  override def priority: Int = -10
}

