package edu.ucdavis.fiehnlab.mona.backend.core.service.persistence

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.service.listener.{PersistenceEvent, PersitenceEventListener}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, PageRequest, Pageable}
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * this defines the MoNA persistence service for spectra and will take care of storing data in the repository
  * as well as providing users with feedback for auditing
  */
@Service
class SpectrumPersistenceService extends LazyLogging {

  /**
    * contains all listeneres in the system to tell subscripers that something with the backend happend
    */
  @Autowired(required = false)
  val persistenceEventListeners: java.util.List[PersitenceEventListener[Spectrum]] = null

  /**
    * provides us with access to all spectra in the mongo database
    */
  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val spectrumElasticRepository: ISpectrumElasticRepositoryCustom = null

  /**
    * will be invoked everytime a spectrum was added to the system
    *
    * @param spectrum
    */
  def fireAddEvent(spectrum: Spectrum) = {
    if (persistenceEventListeners != null) {
      persistenceEventListeners.asScala.foreach(_.added(new PersistenceEvent[Spectrum](spectrum, new Date())))
    }
  }

  /**
    * will be invoked everytime a spectrum was deleted from the system
    *
    * @param spectrum
    */
  def fireDeleteEvent(spectrum: Spectrum) = {
    if (persistenceEventListeners != null) {
      persistenceEventListeners.asScala.foreach(_.deleted(new PersistenceEvent[Spectrum](spectrum, new Date())))
    }
  }

  /**
    * will be invoked everytime a spectrum will be updated in the system
    *
    * @param spectrum
    */
  def fireUpdateEvent(spectrum: Spectrum) = {
    if (persistenceEventListeners != null) {
      persistenceEventListeners.asScala.foreach(_.updated(new PersistenceEvent[Spectrum](spectrum, new Date())))
    }

  }

  /**
    * stores a spectrum in the backend
    *
    * @param spectrum
    * @return
    */
  def add(spectrum: Spectrum) = {
    val s = spectrumMongoRepository.save(spectrum)
    fireAddEvent(s)
  }

  /**
    * updates the provided spectrum
    *
    * @param spectrum
    * @return
    */
  def update(spectrum: Spectrum) = {
    val result = spectrumMongoRepository.save(spectrum)
    fireUpdateEvent(result)
  }

  /**
    * removes the spectrum from the repository
    *
    * @param spectrum
    * @return
    */
  def delete(spectrum: Spectrum) = {
    spectrumMongoRepository.delete(spectrum.id)
    fireDeleteEvent(spectrum)
  }

  /**
    * retrieves the spectrum from the repository
    *
    * @param id
    * @return
    */
  def get(id: String): Spectrum = {
    spectrumMongoRepository.findOne(id)
  }

  /**
    * queries for all the spectra matching this RSQL query
    *
    * @param rsqlQuery
    * @return
    */
  def query(rsqlQuery: String = ""): Iterable[Spectrum] = {

    /**
      * santas little helper
      *
      * @param request
      * @return
      */
    def findDataForQuery(request:PageRequest): Page[Spectrum] = {
      if (rsqlQuery == "") spectrumMongoRepository.findAll(request)
      else this.query(rsqlQuery, request)
    }


    var result = findDataForQuery(new PageRequest(0, 10))
    var it = result.iterator()

    /**
      * used to keep the memory footprint small for large data collection
      */
    new Iterable[Spectrum]() {

      /**
        * defines our custom batch fetching iterator
        *
        * @return
        */
      override def iterator: Iterator[Spectrum] = new Iterator[Spectrum] {
        var currentResult = 0

        override def hasNext: Boolean = {
          it.hasNext
        }

        override def next(): Spectrum = {
          val spectrum = it.next()

          if (!it.hasNext) {
            if (result.getNumber < result.getNumberOfElements) {
              logger.debug("fetching new set of spectra" +
                "")
              result = findDataForQuery(new PageRequest(result.getNumber + 1, 10))
              it = result.iterator()
            }
            else {
              logger.debug("all spectra are fetched!")
            }
          }

          spectrum
        }
      }
    }
  }

  /**
    * does a paginating request to the repository and should be the preferred way to interact with it
    *
    * @param rsqlQuery a well defined RSQL query to be executed
    * @param pageable
    * @return
    */
  def query(rsqlQuery: String, pageable: Pageable): Page[Spectrum] = spectrumElasticRepository.rsqlQuery(rsqlQuery, pageable)

  /**
    * returns the count of all spectra
    *
    * @return
    */
  def count(): Long = spectrumElasticRepository.count()

  /**
    * returns the count matching the given RSQL query
    *
    * @return
    */
  def count(rsqlQuery: String): Long = spectrumElasticRepository.rsqlQueryCount(rsqlQuery)
}
