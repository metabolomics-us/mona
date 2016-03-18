package edu.ucdavis.fiehnlab.mona.backend.core.service.persistence

import java.lang
import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.service.listener.{PersistenceEvent, PersitenceEventListener}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Sort, Page, PageRequest, Pageable}
import org.springframework.data.repository.{PagingAndSortingRepository, CrudRepository}
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    * contains all listeneres in the system to tell subscripers that something with the backend happend
    */
  @Autowired(required = false)
  val persistenceEventListeners: java.util.List[PersitenceEventListener[Spectrum]] = null

  /**
    * provides us with access to all spectra in the mongo database
    */
  @Autowired
  val spectrumMongoRepository: PagingAndSortingRepository[Spectrum, String] with RSQLRepositoryCustom[Spectrum, String] = null

  @Autowired
  val spectrumElasticRepository: PagingAndSortingRepository[Spectrum, String] with RSQLRepositoryCustom[Spectrum, String] = null

  /**
    * will be invoked everytime a spectrum was added to the system
    *
    * @param spectrum
    */
  final def fireAddEvent(spectrum: Spectrum) = {
    logger.trace(s"\t=>\tnotify all listener that the spectrum ${spectrum.id} has been added")
    if (persistenceEventListeners != null) {
      persistenceEventListeners.asScala.sortBy(_.priority).reverse.foreach(_.added(new PersistenceEvent[Spectrum](spectrum, new Date())))
    }
  }

  /**
    * will be invoked everytime a spectrum was deleted from the system
    *
    * @param spectrum
    */
  final def fireDeleteEvent(spectrum: Spectrum) = {
    logger.trace(s"\t=>\tnotify all listener that the spectrum ${spectrum.id} has been deleted")
    if (persistenceEventListeners != null) {
      persistenceEventListeners.asScala.sortBy(_.priority).reverse.foreach(_.deleted(new PersistenceEvent[Spectrum](spectrum, new Date())))
    }
  }

  /**
    * will be invoked everytime a spectrum will be updated in the system
    *
    * @param spectrum
    */
  final def fireUpdateEvent(spectrum: Spectrum) = {
    logger.trace(s"\t=>\tnotify all listener that the spectrum ${spectrum.id} has been updated")
    if (persistenceEventListeners != null) {
      persistenceEventListeners.asScala.sortBy(_.priority).reverse.foreach(_.updated(new PersistenceEvent[Spectrum](spectrum, new Date())))
    }
  }

  /**
    * updates the provided spectrum
    *
    * @param spectrum
    * @return
    */
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
    if (rsqlQuery == "") spectrumMongoRepository.findAll(request)
    //let elastic deal with the request
    else spectrumElasticRepository.rsqlQuery(rsqlQuery, request)
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
  def findAll(rsqlQuery: String): lang.Iterable[Spectrum] = {

    /**
      * generates a new dynamic fetchable
      */
    new DynamicIterable[Spectrum,String](rsqlQuery,fetchSize) {

      /**
        * loads more data from the server for the given query
        */
      override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = findDataForQuery(query,pageable)
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
  def count(): Long = spectrumMongoRepository.count()

  /**
    * returns the count matching the given RSQL query
    *
    * @return
    */
  def count(rsqlQuery: String): Long = spectrumElasticRepository.rsqlQueryCount(rsqlQuery)

  /**
    * delete all objects in the system
    */
  override def deleteAll(): Unit = spectrumMongoRepository.findAll().asScala.foreach(delete(_))

  /**
    * find all spectra with the given id
    *
    * @param ids
    * @return
    */
  override def findAll(ids: lang.Iterable[String]): lang.Iterable[Spectrum] = spectrumMongoRepository.findAll(ids)

  /**
    * kinda inefficient, since it has to find the object first
    *
    * @param id
    */
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
