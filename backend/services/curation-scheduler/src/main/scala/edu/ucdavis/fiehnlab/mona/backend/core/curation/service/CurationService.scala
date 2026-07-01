package edu.ucdavis.fiehnlab.mona.backend.core.curation.service

import java.util.Date
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
  * Created by wohlg on 4/12/2016.
  */
@Service
class CurationService {

  @Autowired
  @Qualifier("spectra-curation-queue")
  val queueName: String = null

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  @Autowired
  val notifications: EventBus[Notification] = null

  @Autowired
  val curationWorkflow: ItemProcessor[Spectrum, Spectrum] = null

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  /**
    * Sends this spectrum to our dedicated queue. This queue can have many consumers to then
    * asynchronously process and curate the object
    *
    * @param spectrum
    */
  def scheduleSpectrum(spectrum: Spectrum): Unit = {
    rabbitTemplate.convertAndSend(queueName, spectrum)
    notifications.sendEvent(Event(Notification(CurationScheduled(spectrum), getClass.getName)))
  }

  /**
    * Loads a single page of spectra and schedules each one for curation
    * Runs in a read-only transaction so a Hibernate session stays open while each spectrum is
    * serialized to the queue, letting lazy associations (e.g. compound) initialize. Kept to one
    * page so the session/connection is released between pages rather than held for the whole run
    *
    * @param query optional query, or null/empty for all spectra
    * @param pageable the page to load and schedule
    * @return the loaded page so the caller can drive pagination
    */
  @Transactional(readOnly = true)
  def scheduleSpectraPage(query: String, pageable: Pageable): Page[Spectrum] = {
    val page: Page[Spectrum] =
      if (query == null || query.isEmpty) {
        spectrumPersistenceService.findAll(pageable)
      } else {
        spectrumPersistenceService.findAll(query, pageable)
      }

    page.forEach(spectrum => scheduleSpectrum(spectrum))
    page
  }

  /**
    * Immediately curate the given spectrum
    * @param spectrum
    * @return
    */
  def curateSpectrum(spectrum: Spectrum): Spectrum = curationWorkflow.process(spectrum)
}

/**
  * simple event to let people who are interested in notifications know that we scheduled one
  *
  * @param spectrum
  * @param time
  */
case class CurationScheduled(spectrum: Spectrum, time: Date = new Date())
