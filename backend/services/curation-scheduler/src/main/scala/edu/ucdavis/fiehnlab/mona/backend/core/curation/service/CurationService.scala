package edu.ucdavis.fiehnlab.mona.backend.core.curation.service

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
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
  }

  /**
    * Loads a single keyset (cursor) page of spectra and schedules each one for curation
    * Walks the id ordering with a cursor (id < lastId) instead of offset paging, so there is no
    * per-page count query and no growing offset on large runs. Runs in a read-only transaction so
    * a Hibernate session stays open while each spectrum is serialized to the queue, letting lazy
    * associations (e.g. compound) initialize
    *
    * @param query  optional query, or null/empty for all spectra
    * @param lastId id of the last spectrum scheduled on the previous page, or null for the first page
    * @param limit  maximum spectra to load and schedule
    * @return the loaded page so the caller can drive the cursor
    */
  @Transactional(readOnly = true)
  def scheduleSpectraKeysetPage(query: String, lastId: String, limit: Int): java.util.List[Spectrum] = {
    val page: java.util.List[Spectrum] = spectrumPersistenceService.findAllForExport(query, lastId, limit)
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
