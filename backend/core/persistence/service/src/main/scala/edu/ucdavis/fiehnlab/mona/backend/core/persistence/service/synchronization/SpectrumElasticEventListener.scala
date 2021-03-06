package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.synchronization

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.{Event, PersistenceEventListener}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by wohlg on 3/15/2016.
  */
@Component
class SpectrumElasticEventListener extends PersistenceEventListener[Spectrum] with LazyLogging {

  @Autowired
  val spectrumElasticRepository: ISpectrumElasticRepositoryCustom = null

  /**
    * an entry was added to the system
    *
    * @param event
    */
  override def added(event: Event[Spectrum]): Unit = {
    logger.debug(s"\t=>\tindexing spectra in elastic search ${event.content.id}")
    spectrumElasticRepository.saveOrUpdate(event.content)
  }

  /**
    * the event was updated in the system
    *
    * @param event
    */
  override def updated(event: Event[Spectrum]): Unit = {
    logger.debug(s"\t=>\treindexing spectra in elastic search ${event.content.id}")
    spectrumElasticRepository.saveOrUpdate(event.content)
  }

  /**
    * an entry was deleted from the system
    *
    * @param event
    */
  override def deleted(event: Event[Spectrum]): Unit = {
    logger.debug(s"\t=>\tremoving spectra from elastic search ${event.content.id}")
    spectrumElasticRepository.delete(event.content)
  }

  /**
    * the priority of the listener
    *
    * @return
    */
  override def priority: Int = 10

  /**
    * requesting a synchronization
    *
    * @param event
    */
  override def sync(event: Event[Spectrum]): Unit = {
    logger.debug(s"\t=>\treindexing spectra in elastic search ${event.content.id}")
    spectrumElasticRepository.saveOrUpdate(event.content)
  }
}
