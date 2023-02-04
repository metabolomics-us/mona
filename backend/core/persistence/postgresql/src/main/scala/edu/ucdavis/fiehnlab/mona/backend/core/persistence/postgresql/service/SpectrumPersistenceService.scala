package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.{Event, EventScheduler}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import org.springframework.beans.factory.annotation.Autowired
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumRepository
import com.turkraft.springfilter.boot.FilterSpecification
import org.springframework.cache.annotation.{CacheEvict, Cacheable}
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.{Page, PageRequest, Pageable, Sort}
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service

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
  @CacheEvict(value = Array("spectra"))
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
  @CacheEvict(value = Array("spectra"))
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
  @CacheEvict(value = Array("spectra"))
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
  @Cacheable(value = Array("spectra"))
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
  @Cacheable(value = Array("spectra"))
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
  @Cacheable(value = Array("spectra"))
  def count(): Long = spectrumResultRepository.count()

  /**
   * returns the count matching the given RSQL query
   *
   * @return
   */
  @Cacheable(value = Array("spectra"))
  def count(query: String): Long = {
    val spec: Specification[Spectrum] = new FilterSpecification[Spectrum](query)
    val count: Long = spectrumResultRepository.count(spec)
    count
  }

  @CacheEvict(value = Array("spectra"), allEntries = true)
  def deleteSpectraByIdIn(ids: java.util.List[String]): Unit = {
    spectrumResultRepository.findAllByIdIn(ids).asScala.foreach(delete)
  }

  @CacheEvict(value = Array("spectra"), allEntries = true)
  def deleteSpectraByQuery(query: String): Unit = {
    val spec: Specification[Spectrum] = new FilterSpecification[Spectrum](query)
    spectrumResultRepository.findAll(spec).asScala.foreach(delete)
  }

  /**
   * delete all objects in the system
   */
  @CacheEvict(value = Array("spectra"), allEntries = true)
  def deleteAll(): Unit = spectrumResultRepository.findAll().asScala.foreach(delete)

  /**
   * find all spectra with the given id
   *
   * @param ids
   * @return
   */
  @Cacheable(value = Array("spectra"))
  def findAll(ids: java.util.List[String]): java.util.List[Spectrum] = spectrumResultRepository.findAllByIdIn(ids)

  /**
   * checks if the given id exist in the database
   *
   * @param id
   * @return
   */
  @Cacheable(value = Array("spectra"))
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


  @CacheEvict(value = Array("spectra"))
  def deleteById(id: String): Unit = spectrumResultRepository.deleteById(id)

  @CacheEvict(value = Array("spectra"), allEntries = true)
  def deleteAll(entities: lang.Iterable[_ <: Spectrum]): Unit = spectrumResultRepository.deleteAll(entities)

  //def saveAll(spectra: List[SpectrumResult]): Unit = spectrumResultRepository.saveAll(spectra)
  def saveAll[S <: Spectrum](entities: lang.Iterable[S]): lang.Iterable[S] = {
    entities.asScala.collect {
      case s: S => save(s)
    }.asJava
  }
}
