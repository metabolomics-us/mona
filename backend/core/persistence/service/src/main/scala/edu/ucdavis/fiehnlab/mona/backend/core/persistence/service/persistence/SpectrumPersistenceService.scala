package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence

import java.lang
import java.util.{Date, Optional}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.{Event, EventScheduler}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.counter.SequenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.{CacheEvict, Cacheable}
import org.springframework.data.domain.{Page, Pageable, Sort}
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * Defines the MoNA persistence service for spectra and will take care of storing data in the repository
  * as well as providing users with feedback for auditing
  */
@Service
class SpectrumPersistenceService extends LazyLogging with PagingAndSortingRepository[Spectrum, String] {

  /**
    * how many results to fetch at a time
    */
  val fetchSize = 10

  /**
    * Provides us with access to all spectra in the mongo database
    */
  @Autowired
  val spectrumMongoRepository: PagingAndSortingRepository[Spectrum, String] with RSQLRepositoryCustom[Spectrum, String] = null

  @Autowired
  val spectrumElasticRepository: PagingAndSortingRepository[Spectrum, String] with RSQLRepositoryCustom[Spectrum, String] = null

  @Autowired
  val sequenceService: SequenceService = null


  @Autowired(required = false)
  val eventScheduler: EventScheduler[Spectrum] = null

  /**
    * will be invoked everytime a spectrum was added to the system
    *
    * @param spectrum
    */
  final def fireAddEvent(spectrum: Spectrum): Unit = {
    logger.trace(s"\t=>\tnotify all listener that the spectrum ${spectrum.id} has been added")
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
    logger.trace(s"\t=>\tnotify all listener that the spectrum ${spectrum.id} has been deleted")
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
    logger.trace(s"\t=>\tnotify all listener that the spectrum ${spectrum.id} has been updated")

    if (eventScheduler != null) {
      eventScheduler.scheduleEventProcessing(Event[Spectrum](spectrum, new Date, Event.UPDATE))
    }
  }

  final def fireSyncEvent(spectrum: Spectrum): Unit = {
    logger.trace(s"\t=>\tnotify all listener that the spectrum ${spectrum.id} has been scheduled for synchronization")

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
  final def update(spectrum: Spectrum): Spectrum = {
    val result = spectrumMongoRepository.save(spectrum.copy(lastUpdated = new Date()))
    fireUpdateEvent(result)
    result
  }

  /**
    *
    * @param entity
    * @tparam S
    * @return
    */
  @CacheEvict(value = Array("spectra"))
  final override def save[S <: Spectrum](entity: S): S = {
    val transformedEntity = entity.copy(
      id = Option(entity.id).getOrElse(sequenceService.getNextMoNAID),
      dateCreated = Option(entity.dateCreated).getOrElse(new Date),
      lastUpdated = new Date
    )

    val result = spectrumMongoRepository.save(transformedEntity).asInstanceOf[S]
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
    spectrumMongoRepository.delete(spectrum)
    fireDeleteEvent(spectrum)
  }

  /**
    * retrieves the spectrum from the repository
    *
    * @param id
    * @return
    */
  @Cacheable(value = Array("spectra"))
  def findById(id: String): Optional[Spectrum] = spectrumMongoRepository.findById(id)

  /**
    *
    * @param request
    * @return
    */
  private def findDataForQuery(rsqlQuery: String, textQuery: String, request: Pageable): Page[Spectrum] = {
    logger.debug(s"executing query: \n$rsqlQuery\n")

    if ((rsqlQuery == null || rsqlQuery == "") && (textQuery == null || textQuery == "")) {
      // No need to hit elastic here, since no query is executed
      spectrumMongoRepository.findAll(request)
    } else {
      // Perform RSQL query in elastic
      spectrumElasticRepository.query(rsqlQuery, textQuery, request)
    }
  }

  /**
    * find all data without a query
    *
    * @return
    */
  def findAll(): lang.Iterable[Spectrum] = findAll("", "")

  /**
    * fires a synchronization event, so that system updates all it's clients. Be aware that this is very expensive!
    */
  def forceSynchronization(): Unit = findAll().iterator().asScala.foreach(fireSyncEvent)


  /**
    * queries for all the spectra matching this query
    *
    * @param rsqlQuery
    * @param textQuery
    * @return
    */
  @Cacheable(value = Array("spectra"))
  def findAll(rsqlQuery: String, textQuery: String): lang.Iterable[Spectrum] = {

    /**
      * generates a new dynamic fetchable
      */
    new DynamicIterable[Spectrum, String](rsqlQuery, fetchSize) {

      /**
        * loads more data from the server for the given query
        */
      override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = findDataForQuery(query, textQuery, pageable)
    }
  }

  /**
    * does a paginating request to the repository and should be the preferred way to interact with it
    *
    * @param query     a RSQL or text query
    * @param textQuery a full text query
    * @param pageable
    * @return
    */
  def findAll(query: String, textQuery: String, pageable: Pageable): Page[Spectrum] = findDataForQuery(query, textQuery, pageable)

  /**
    * returns the count of all spectra
    *
    * @return
    */
  @Cacheable(value = Array("spectra"))
  def count(): Long = spectrumMongoRepository.count()

  /**
    * returns the count matching the given RSQL query
    *
    * @return
    */
  @Cacheable(value = Array("spectra"))
  def count(query: String, textQuery: String): Long = {
    spectrumElasticRepository.queryCount(query, textQuery)
  }

  /**
    * delete all objects in the system
    */
  @CacheEvict(value = Array("spectra"), allEntries = true)
  override def deleteAll(): Unit = spectrumMongoRepository.findAll().asScala.foreach(delete)

  /**
    * find all spectra with the given id
    *
    * @param ids
    * @return
    */
  @Cacheable(value = Array("spectra"))
  override def findAllById(ids: lang.Iterable[String]): lang.Iterable[Spectrum] = spectrumMongoRepository.findAllById(ids)

  /**
    * kinda inefficient, since it has to find the object first
    *
    * @param id
    */
  @CacheEvict(value = Array("spectra"))
  final override def deleteById(id: String): Unit = {
    val spectrum = findById(id)

    if (spectrum.isPresent) {
      delete(spectrum.get())
    }
  }

  /**
    * delete all entities for the spectrum
    *
    * @param entities
    */
  override def deleteAll(entities: lang.Iterable[_ <: Spectrum]): Unit = {
    val iterator = entities.iterator()

    while (iterator.hasNext) {
      delete(iterator.next())
    }
  }


  override def saveAll[S <: Spectrum](entities: lang.Iterable[S]): lang.Iterable[S] =
    entities.asScala.map(save).asJava

  /**
    * checks if the given id exist in the database
    *
    * @param id
    * @return
    */
  @Cacheable(value = Array("spectra"))
  override def existsById(id: String): Boolean = spectrumMongoRepository.existsById(id)

  /**
    * finds all data with sorting.
    *
    * @param sort
    * @return
    */
  override def findAll(sort: Sort): lang.Iterable[Spectrum] = spectrumMongoRepository.findAll(sort)

  /**
    * finds all with pagination
    *
    * @param pageable
    * @return
    */
  override def findAll(pageable: Pageable): Page[Spectrum] = findAll("", "", pageable)
}
