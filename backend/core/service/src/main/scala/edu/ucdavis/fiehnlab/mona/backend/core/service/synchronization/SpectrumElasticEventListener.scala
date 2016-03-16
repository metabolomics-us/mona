package edu.ucdavis.fiehnlab.mona.backend.core.service.synchronization

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.service.listener.{PersistenceEvent, PersitenceEventListener}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by wohlg on 3/15/2016.
  */
@Component
class SpectrumElasticEventListener extends PersitenceEventListener[Spectrum] {

  @Autowired
  val spectrumElasticRepository: ISpectrumElasticRepositoryCustom = null

  /**
    * an entry was added to the system
    *
    * @param event
    */
  override def added(event: PersistenceEvent[Spectrum]): Unit = spectrumElasticRepository.save(event.content)

  /**
    * the event was updated in the system
    *
    * @param event
    */
  override def updated(event: PersistenceEvent[Spectrum]): Unit = {
    spectrumElasticRepository.saveOrUpdate(event.content)
  }

  /**
    * an entry was deleted from the system
    *
    * @param event
    */
  override def deleted(event: PersistenceEvent[Spectrum]): Unit = spectrumElasticRepository.delete(event.content)
}
