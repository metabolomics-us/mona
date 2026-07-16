package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{DeletionJob, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.{Event, EventScheduler}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import org.springframework.beans.factory.annotation.Autowired
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{DeletionJobRepository, SpectrumRepository}
import com.turkraft.springfilter.boot.FilterSpecification
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.{Page, PageRequest, Pageable, Sort}
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

import javax.persistence.EntityManager
import java.lang
import java.util.{Date, List}
import scala.jdk.CollectionConverters._

@Service
@Profile(Array("mona.persistence"))
class SpectrumPersistenceService extends LazyLogging {

  val fetchSize = 10

  @Autowired
  val spectrumResultRepository: SpectrumRepository = null

  @Autowired
  val objectMapper: ObjectMapper = null

//  @Autowired
//  val sequenceService: SequenceService = null

  @Autowired(required = false)
  val eventScheduler: EventScheduler[Spectrum] = null

  @Autowired(required = false)
  val deletionJobRepository: DeletionJobRepository = null

  @Autowired
  private val entityManager: EntityManager = null

  @Autowired
  private val transactionManager: PlatformTransactionManager = null

  // Built lazily (once transactionManager is injected) rather than via @PostConstruct, since a
  // lazy val already guarantees single, on-first-use initialization
  private lazy val transactionTemplate = new TransactionTemplate(transactionManager)

  // Spectra deleted per batch before the persistence context is cleared. Keeping the context
  // small is what avoids the O(n^2) dirty checking that throttled the old delete loop
  val deletionBatchSize = 500

  final def fireAddEvent(spectrum: Spectrum): Unit = {
    logger.debug(s"\t=>\tnotify all listener that the spectrum ${spectrum.getId} has been added")
    if (eventScheduler != null) {
      eventScheduler.scheduleEventProcessing(Event[Spectrum](spectrum, new Date, Event.ADD))
    }
  }

  /**
   * will be invoked everytime a spectrum was deleted from the system
   *
   * @param spectrum
   */
  final def fireDeleteEvent(spectrum: Spectrum): Unit = {
    logger.debug(s"\t=>\tnotify all listener that the spectrum ${spectrum.getId} has been deleted")
    if (eventScheduler != null) {
      eventScheduler.scheduleEventProcessing(Event[Spectrum](spectrum, new Date, Event.DELETE))
    }
  }

  /**
   * will be invoked everytime a spectrum will be updated in the system
   *
   * @param spectrum
   */
  final def fireUpdateEvent(spectrum: Spectrum): Unit = {
    logger.debug(s"\t=>\tnotify all listener that the spectrum ${spectrum.getId} has been updated")

    if (eventScheduler != null) {
      eventScheduler.scheduleEventProcessing(Event[Spectrum](spectrum, new Date, Event.UPDATE))
    }
  }

  final def fireSyncEvent(spectrum: Spectrum): Unit = {
    logger.debug(s"\t=>\tnotify all listener that the spectrum ${spectrum.getId} has been scheduled for synchronization")

    if (eventScheduler != null) {
      eventScheduler.scheduleEventProcessing(Event[Spectrum](spectrum, new Date, Event.SYNC))
    }
  }

  /**
   * updates the provided spectrum
   *
   * @param spectrum
   * @return
   */
  final def update(spectrum: Spectrum): Unit = {
    spectrum.setLastUpdated(new Date())
    val result = spectrumResultRepository.save(spectrum)

    fireUpdateEvent(result)
//    result
  }

  /**
   *
   * @param entity
   * @tparam S
   * @return
   */
  final def save[S <: Spectrum](entity: S): S = {
    val result = spectrumResultRepository.save(entity)
    fireAddEvent(result)
    result
  }

  /**
   * updates the given spectra
   *
   * @param spectra
   */
  def update(spectra: lang.Iterable[Spectrum]): Unit = spectra.asScala.foreach(update)

  /**
   * removes the spectrum from the repository
   *
   * @param spectrum
   * @return
   */
  final def delete(spectrum: Spectrum): Unit = {
    spectrumResultRepository.delete(spectrum)
    spectrumResultRepository.flush()
    fireDeleteEvent(spectrum)
  }

  /**
   * retrieves the spectrum from the repository
   *
   * @param id
   * @return
   */
  def findByMonaId(id: String): Spectrum = spectrumResultRepository.findById(id).orElse(null)

  /**
   *
   * @param request
   * @return
   */
  private def findDataForQuery(query: String, request: Pageable): Page[Spectrum] = {
    logger.debug(s"executing query: \n$query\n")
    val spec: Specification[Spectrum] = new FilterSpecification[Spectrum](query)
    val rez: Page[Spectrum] = spectrumResultRepository.findAll(spec, PageRequest.of(request.getPageNumber, request.getPageSize, Sort.by("id").descending()))
    rez
  }

  private def findDataForEmptyQuery(request: Pageable): Page[Spectrum] = {
    logger.debug(s"executing empty query to findAll")
    spectrumResultRepository.findAll(PageRequest.of(request.getPageNumber, request.getPageSize, Sort.by("id").descending()))
  }

  /**
   * Keyset (cursor) page of spectra for exports, ordered by id descending. Returns up to limit
   * spectra with id less than lastId (pass null for the first page)
   *
   * @param query  the filter query, or null/empty for all spectra
   * @param lastId id of the last spectrum already returned, or null for the first page
   * @param limit  maximum spectra to return
   * @return the next keyset page of spectra
   */
  def findAllForExport(query: String, lastId: String, limit: Int): List[Spectrum] =
    spectrumResultRepository.findForExport(query, lastId, limit)

  /**
   * find all data without a query
   *
   * @return
   */
  def findAll(): lang.Iterable[Spectrum] = {
    new DynamicIterable[Spectrum, String]("", fetchSize) {

      /**
       * loads more data from the server for the given query
       */
      override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = findDataForEmptyQuery(pageable)
    }

  }

  /**
   * fires a synchronization event, so that system updates all it's clients. Be aware that this is very expensive!
   */
  def forceSynchronization(): Unit = findAll().asScala.foreach{ x =>
    fireSyncEvent(x)
  }
  //def forceSynchronization(): Unit = findAll().iterator().asScala.foreach(fireSyncEvent)


  /**
   * queries for all the spectra matching this query
   *
   * @param query
   * @return
   */
  def findAll(query: String): lang.Iterable[Spectrum] = {

    /**
     * generates a new dynamic fetchable
     */
    val test = new DynamicIterable[Spectrum, String](query, fetchSize) {

      /**
       * loads more data from the server for the given query
       */
      override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = findDataForQuery(query, pageable)
    }
    test
  }

  /**
   * does a paginating request to the repository and should be the preferred way to interact with it
   *
   * @param query     a RSQL or text query
   * @param pageable
   * @return
   */
  def findAll(query: String, pageable: Pageable): Page[Spectrum] = findDataForQuery(query, pageable)

  /**
   * returns the count of all spectra
   *
   * @return
   */
  def count(): Long = spectrumResultRepository.count()

  /**
   * returns the count matching the given RSQL query
   *
   * @return
   */
  def count(query: String): Long = {
    val spec: Specification[Spectrum] = new FilterSpecification[Spectrum](query)
    val count: Long = spectrumResultRepository.count(spec)
    count
  }

  def deleteSpectraByIdIn(ids: java.util.List[String]): Unit = {
    spectrumResultRepository.findAllByIdIn(ids).asScala.foreach(delete)
  }

  def deleteSpectraByQuery(query: String): Unit = {
    val spec: Specification[Spectrum] = new FilterSpecification[Spectrum](query)
    spectrumResultRepository.findAll(spec).asScala.foreach(delete)
  }

  /**
   * deletes every spectrum matching the query in bounded batches, recording progress on the job.
   * Spectra that fail to delete (e.g. a corrupt row) are skipped and logged rather than aborting
   * the whole job. Paging always reads page zero of the still matching set, excluding ids that
   * were skipped, so the set shrinks to empty without re-processing skipped rows
   *
   * @param query a RSQL or text query, must be non empty (an empty query would match everything)
   * @param job   the tracking row whose deleted/skipped counters are updated as work progresses
   */
  def deleteSpectraByQueryTracked(query: String, job: DeletionJob): Unit = {
    val baseSpec: Specification[Spectrum] = new FilterSpecification[Spectrum](query)
    val skipped = scala.collection.mutable.Set[String]()
    var hasMore = true

    while (hasMore) {
      val spec: Specification[Spectrum] =
        if (skipped.isEmpty) baseSpec
        else baseSpec.and((root, _, cb) => cb.not(root.get[String]("id").in(skipped.asJava)))

      val batch = spectrumResultRepository
        .findAll(spec, PageRequest.of(0, deletionBatchSize, Sort.by("id").descending()))
        .getContent.asScala

      if (batch.isEmpty) {
        hasMore = false
      } else {
        deleteBatch(batch, job, skipped)
        saveJobProgress(job)
      }
    }
  }

  /**
   * deletes the spectra with the given ids in bounded batches, recording progress on the job.
   * Missing or un-deletable ids are skipped and logged
   *
   * @param ids the mona ids to delete
   * @param job the tracking row whose deleted/skipped counters are updated as work progresses
   */
  def deleteSpectraByIdsTracked(ids: java.util.List[String], job: DeletionJob): Unit = {
    val skipped = scala.collection.mutable.Set[String]()

    spectrumResultRepository.findAllByIdIn(ids).asScala.grouped(deletionBatchSize).foreach { batch =>
      deleteBatch(batch, job, skipped)
      saveJobProgress(job)
    }
  }

  /**
   * deletes a batch of spectra. Tries the fast path first, if anything in the batch fails, the whole
   * transaction rolls back (nothing partially deleted) and the batch is retried row by row, each on
   * its own auto-committed transaction, so a single bad row can't take down the rest of the batch.
   */
  private def deleteBatch(batch: Iterable[Spectrum], job: DeletionJob, skipped: scala.collection.mutable.Set[String]): Unit = {
    val fastPathSucceeded =
      try {
        deleteBatchFast(batch)
        true
      } catch {
        case e: Exception =>
          logger.warn(s"batched delete failed for a batch of ${batch.size} spectra, retrying row by row: ${e.getMessage}")
          entityManager.clear()
          false
      }

    if (fastPathSucceeded) {
      batch.foreach { spectrum =>
        fireDeleteEvent(spectrum)
        job.setDeleted(job.getDeleted + 1)
      }
    } else {
      deleteBatchSlow(batch, job, skipped)
    }

    entityManager.clear()
  }

  /**
   * deletes every spectrum in the batch via the normal Hibernate entity delete, all within a single
   * transaction. This changes only how many commits the batch costs, not what gets deleted or how
   */
  private def deleteBatchFast(batch: Iterable[Spectrum]): Unit = {
    transactionTemplate.execute { _ =>
      batch.foreach(spectrumResultRepository.delete)
    }
  }

  /**
   * deletes a batch one row at a time, each on its own auto-committed transaction, so a failure on
   * one row is isolated. Used as a fallback when the batched fast path fails
   */
  private def deleteBatchSlow(batch: Iterable[Spectrum], job: DeletionJob, skipped: scala.collection.mutable.Set[String]): Unit = {
    batch.foreach { spectrum =>
      try {
        spectrumResultRepository.delete(spectrum)
        spectrumResultRepository.flush()
        fireDeleteEvent(spectrum)
        job.setDeleted(job.getDeleted + 1)
      } catch {
        case e: Exception =>
          logger.error(s"failed to delete spectrum ${spectrum.getId}, skipping: ${e.getMessage}", e)
          skipped += spectrum.getId
          job.setSkipped(job.getSkipped + 1)
          // recover the persistence context from a failed flush so the next row is unaffected
          entityManager.clear()
      }
    }
  }

  private def saveJobProgress(job: DeletionJob): Unit = {
    if (deletionJobRepository != null) {
      job.setLastUpdated(new Date())
      deletionJobRepository.save(job)
    }
  }

  /**
   * delete all objects in the system
   */
  def deleteAll(): Unit = spectrumResultRepository.findAll().asScala.foreach(delete)

  /**
   * find all spectra with the given id
   *
   * @param ids
   * @return
   */
  def findAll(ids: java.util.List[String]): java.util.List[Spectrum] = spectrumResultRepository.findAllByIdIn(ids)

  /**
   * checks if the given id exist in the database
   *
   * @param id
   * @return
   */
  def existsById(id: String): Boolean = spectrumResultRepository.existsById(id)

  /**
   * finds all data with sorting.
   *
   * @param sort
   * @return
   */
  def findAll(sort: Sort): List[Spectrum] = spectrumResultRepository.findAll(sort)

  /**
   * finds all with pagination
   *
   * @param pageable
   * @return
   */
  def findAll(pageable: Pageable): Page[Spectrum] = spectrumResultRepository.findAll(pageable)


  def deleteById(id: String): Unit = spectrumResultRepository.deleteById(id)

  def deleteAll(entities: lang.Iterable[_ <: Spectrum]): Unit = spectrumResultRepository.deleteAll(entities)

  //def saveAll(spectra: List[SpectrumResult]): Unit = spectrumResultRepository.saveAll(spectra)
  def saveAll[S <: Spectrum](entities: lang.Iterable[S]): lang.Iterable[S] = {
    entities.asScala.collect {
      case s: S => save(s)
    }.asJava
  }
}
