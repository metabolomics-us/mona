package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.synchronization

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.{Event, PersistenceEventListener}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component}
import org.springframework.context.annotation.Profile

/**
 * Created by wohlgemuth on 3/17/16.
 */
@Component
@Profile(Array("mona.persistence"))
class CountListener extends PersistenceEventListener[SpectrumResult] with LazyLogging {
  @Autowired
  val spectrumResultRepository: SpectrumResultRepository = null

  /**
   * an entry was added to the system
   *
   * @param event
   */
  override def added(event: Event[SpectrumResult]): Unit = {
    logger.debug(s"added spectrum count is now ${spectrumResultRepository.count()}")
  }

  /**
   * the event was updated in the system
   *
   * @param event
   */
  override def updated(event: Event[SpectrumResult]): Unit = {
    logger.debug(s"updated spectrum count is now ${spectrumResultRepository.count()}")
  }

  /**
   * an entry was deleted from the system
   *
   * @param event
   */
  override def deleted(event: Event[SpectrumResult]): Unit = {
    logger.debug(s"deleted spectrum count is now ${spectrumResultRepository.count()}")
  }

  /**
   * the priority of the listener
   *
   * @return
   */
  override def priority: Int = -10
}

