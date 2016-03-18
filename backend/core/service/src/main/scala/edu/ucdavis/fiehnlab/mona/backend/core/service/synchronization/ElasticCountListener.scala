package edu.ucdavis.fiehnlab.mona.backend.core.service.synchronization

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.service.listener.{PersistenceEvent, PersitenceEventListener}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 3/17/16.
  */
@Component
class ElasticCountListener  extends PersitenceEventListener[Spectrum] with LazyLogging{
  @Autowired
  val spectrumElasticRepository: ISpectrumElasticRepositoryCustom = null

  /**
    * an entry was added to the system
    *
    * @param event
    */
  override def added(event: PersistenceEvent[Spectrum]): Unit = {
    logger.debug(s"added spectrum count is now ${spectrumElasticRepository.count()}")
  }

  /**
    * the event was updated in the system
    *
    * @param event
    */
  override def updated(event: PersistenceEvent[Spectrum]): Unit = {
    logger.debug(s"updated spectrum count is now ${spectrumElasticRepository.count()}")
  }

  /**
    * an entry was deleted from the system
    *
    * @param event
    */
  override def deleted(event: PersistenceEvent[Spectrum]): Unit = {
    logger.debug(s"deleted spectrum count is now ${spectrumElasticRepository.count()}")
  }

  /**
    * the priority of the listener
    *
    * @return
    */
  override def priority: Int = -10
}
