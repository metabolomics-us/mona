package edu.ucdavis.fiehnlab.mona.backend.core.service.synchronization

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.service.listener.{PersistenceEvent, PersitenceEventListener}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by wohlg on 3/15/2016.
  */
@Component
class SpectrumElasticEventListener extends PersitenceEventListener[Spectrum]with LazyLogging {

  @Autowired
  val spectrumElasticRepository: ISpectrumElasticRepositoryCustom = null

  /**
    * an entry was added to the system
    *
    * @param event
    */
  override def added(event: PersistenceEvent[Spectrum]): Unit = {
    logger.debug(s"\t=>\tindexing spectra in elastic search ${event.content.id}")
    spectrumElasticRepository.save(event.content)
  }

  /**
    * the event was updated in the system
    *
    * @param event
    */
  override def updated(event: PersistenceEvent[Spectrum]): Unit = {
    logger.debug(s"\t=>\treindexing spectra in elastic search ${event.content.id}")
    spectrumElasticRepository.saveOrUpdate(event.content)
  }

  /**
    * an entry was deleted from the system
    *
    * @param event
    */
  override def deleted(event: PersistenceEvent[Spectrum]): Unit = {
    logger.debug(s"\t=>\tremoving spectra from elastic search ${event.content.id}")
    spectrumElasticRepository.delete(event.content)
  }
}