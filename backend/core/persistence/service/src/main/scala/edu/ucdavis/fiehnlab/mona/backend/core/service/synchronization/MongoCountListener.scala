package edu.ucdavis.fiehnlab.mona.backend.core.service.synchronization

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.service.listener.{PersistenceEvent, PersistenceEventListener}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 3/17/16.
  */
@Component
class MongoCountListener extends PersistenceEventListener[Spectrum] with LazyLogging{
  @Autowired
  val spectrumMongoRepository: PagingAndSortingRepository[Spectrum, String] with RSQLRepositoryCustom[Spectrum, String] = null

  /**
    * an entry was added to the system
    *
    * @param event
    */
  override def added(event: PersistenceEvent[Spectrum]): Unit = {
    logger.debug(s"added spectrum count is now ${spectrumMongoRepository.count()}")
  }

  /**
    * the event was updated in the system
    *
    * @param event
    */
  override def updated(event: PersistenceEvent[Spectrum]): Unit = {
    logger.debug(s"updated spectrum count is now ${spectrumMongoRepository.count()}")
  }

  /**
    * an entry was deleted from the system
    *
    * @param event
    */
  override def deleted(event: PersistenceEvent[Spectrum]): Unit = {
    logger.debug(s"deleted spectrum count is now ${spectrumMongoRepository.count()}")
  }

  /**
    * the priority of the listener
    *
    * @return
    */
  override def priority: Int = -10
}
