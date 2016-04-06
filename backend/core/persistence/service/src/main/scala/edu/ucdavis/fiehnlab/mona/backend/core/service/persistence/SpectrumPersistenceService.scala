package edu.ucdavis.fiehnlab.mona.backend.core.service.persistence

import java.lang
import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.service.listener._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.{CacheEvict, Cacheable}
import org.springframework.data.domain.{Page, Pageable, Sort}
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * this defines the MoNA persistence service for spectra and will take care of storing data in the repository
  * as well as providing users with feedback for auditing
  */
@Service
class SpectrumPersistenceService extends LazyLogging with PagingAndSortingRepository[Spectrum, String] {

  /**
    * how many results to fetch at a time
    */
  val fetchSize = 10

  /**
    * provides us with access to all spectra in the mongo database
    */
  @Autowired
  val spectrumMongoRepository: PagingAndSortingRepository[Spectrum, String] with RSQLRepositoryCustom[Spectrum, String] = null

  @Autowired
  val spectrumElasticRepository: PagingAndSortingRepository[Spectrum, String] with RSQLRepositoryCustom[Spectrum, String] = null

  @Autowired(required = false)
  val eventScheduler: EventScheduler[Spectrum] = null

  /**
    * will be invoked everytime a spectrum was added to the system
    *
    * @param spectrum
    */
  final def fireAddEvent(spectrum: Spectrum) = {
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
  final def fireDeleteEvent(spectrum: Spectrum) = {
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
  final def fireUpdateEvent(spectrum: Spectrum) = {
    logger.trace(s"\t=>\tnotify all listener that the spectrum ${spectrum.id} has been updated")
    if (eventScheduler != null) {
      eventScheduler.scheduleEventProcessing(Event[Spectrum](spectrum, new Date, Event.UPDATE))
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
    val result = spectrumMongoRepository.save(spectrum)
    fireUpdateEvent(result)
    result
  }

  /**
    * updates the given spectra
    *
    * @param spectra
    */
  def update(spectra: lang.Iterable[Spectrum]): Unit = spectra.asScala.foreach(update(_))

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
  def findOne(id: String): Spectrum = {
    spectrumMongoRepository.findOne(id)
  }

  /**
    * santas little helper
    *
    * @param request
    * @return
    */
  private def findDataForQuery(rsqlQuery: String, request: Pageable): Page[Spectrum] = {
    //no need to hit elastic here, since no qury is executed
    if (rsqlQuery == "") {
      spectrumMongoRepository.findAll(request)
    }
    //let elastic deal with the request
    else {
      spectrumElasticRepository.rsqlQuery(rsqlQuery, request)
    }
  }

  /**
    * find all data without a query
    *
    * @return
    */
  def findAll(): lang.Iterable[Spectrum] = findAll("")

  /**
    * queries for all the spectra matching this RSQL query
    *
    * @param rsqlQuery
    * @return
    */
  @Cacheable(value = Array("spectra"))
  def findAll(rsqlQuery: String): lang.Iterable[Spectrum] = {

    /**
      * generates a new dynamic fetchable
      */
    new DynamicIterable[Spectrum, String](rsqlQuery, fetchSize) {

      /**
        * loads more data from the server for the given query
        */
      override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = findDataForQuery(query, pageable)
    }
  }

  /**
    * does a paginating request to the repository and should be the preferred way to interact with it
    *
    * @param rsqlQuery a well defined RSQL query to be executed
    * @param pageable
    * @return
    */
  def findAll(rsqlQuery: String, pageable: Pageable): Page[Spectrum] = findDataForQuery(rsqlQuery, pageable)

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
  def count(rsqlQuery: String): Long = spectrumElasticRepository.rsqlQueryCount(rsqlQuery)

  /**
    * delete all objects in the system
    */
  @CacheEvict(value = Array("spectra"), allEntries = true)
  override def deleteAll(): Unit = spectrumMongoRepository.findAll().asScala.foreach(delete(_))

  /**
    * find all spectra with the given id
    *
    * @param ids
    * @return
    */
  @Cacheable(value = Array("spectra"))
  override def findAll(ids: lang.Iterable[String]): lang.Iterable[Spectrum] = spectrumMongoRepository.findAll(ids)

  /**
    * kinda inefficient, since it has to find the object first
    *
    * @param id
    */
  @CacheEvict(value = Array("spectra"))
  final override def delete(id: String): Unit = {
    val spectrum = findOne(id)
    delete(spectrum)
  }

  /**
    * delete all entities for the spectrym
    *
    * @param entities
    */
  override def delete(entities: lang.Iterable[_ <: Spectrum]): Unit = {
    val iterator = entities.iterator()

    while (iterator.hasNext) {
      delete(iterator.next())
    }
  }

  /**
    *
    * @param entity
    * @tparam S
    * @return
    */
  @CacheEvict(value = Array("spectra"))
  final override def save[S <: Spectrum](entity: S): S = {
    val result = spectrumMongoRepository.save(entity)
    fireAddEvent(result)
    result
  }

  override def save[S <: Spectrum](entities: lang.Iterable[S]): lang.Iterable[S] = {
    entities.asScala.collect {
      case x: S => save(x)
    }.asJava
  }

  /**
    * checks if the given id exist in the database
    *
    * @param id
    * @return
    */
  @Cacheable(value = Array("spectra"))
  override def exists(id: String): Boolean = spectrumMongoRepository.exists(id)

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
  override def findAll(pageable: Pageable): Page[Spectrum] = findAll("", pageable)
}
