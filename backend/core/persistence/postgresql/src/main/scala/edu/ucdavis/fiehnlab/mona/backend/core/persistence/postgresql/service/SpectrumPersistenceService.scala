package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import cz.jirutka.rsql.parser.RSQLParser
import cz.jirutka.rsql.parser.ast.{ComparisonOperator, Node}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.{Event, EventScheduler}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.SearchTable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.{SpectrumResult, SpectrumResultId}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.SearchTableRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.SearchTableRepository.SparseSearchTable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.rsql.{CustomRsqlVisitor, RSQLOperatorsCustom}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.{CacheEvict, Cacheable}
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.{Page, PageRequest, Pageable, Sort}
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import scala.collection.mutable.ListBuffer

import java.lang
import java.util.{Date, List}
import scala.jdk.CollectionConverters._

@Service
@Profile(Array("mona.persistence"))
class SpectrumPersistenceService extends LazyLogging {

  val fetchSize = 10

  @Autowired
  val spectrumResultRepository: SpectrumResultRepository = null

  @Autowired
  val searchTableRepository: SearchTableRepository = null

  @Autowired
  val objectMapper: ObjectMapper = null

  @Autowired
  val sequenceService: SequenceService = null

  @Autowired(required = false)
  val eventScheduler: EventScheduler[SpectrumResult] = null

  @Autowired(required = false)
  val eventSchedulerSparse: EventScheduler[SparseSearchTable] = null

  val operators: java.util.Set[ComparisonOperator] = RSQLOperatorsCustom.newDefaultOperators()

  final def fireAddEvent(spectrumResult: SpectrumResult): Unit = {
    logger.debug(s"\t=>\tnotify all listener that the spectrum ${spectrumResult.getMonaId} has been added")
    if (eventScheduler != null) {
      eventScheduler.scheduleEventProcessing(Event[SpectrumResult](spectrumResult, new Date, Event.ADD))
    }
  }

  /**
   * will be invoked everytime a spectrum was deleted from the system
   *
   * @param spectrum
   */
  final def fireDeleteEvent(spectrumResult: SpectrumResult): Unit = {
    logger.debug(s"\t=>\tnotify all listener that the spectrum ${spectrumResult.getMonaId} has been deleted")
    if (eventScheduler != null) {
      eventScheduler.scheduleEventProcessing(Event[SpectrumResult](spectrumResult, new Date, Event.DELETE))
    }
  }

  final def fireDeleteEvent(spectrumSparse: SparseSearchTable): Unit = {
    logger.debug(s"\t=>\tnotify all listener that the spectrum ${spectrumSparse.getMonaId} has been deleted")
    if (eventSchedulerSparse != null) {
      eventSchedulerSparse.scheduleEventProcessing(Event[SparseSearchTable](spectrumSparse, new Date, Event.DELETE))
    }
  }

  /**
   * will be invoked everytime a spectrum will be updated in the system
   *
   * @param spectrum
   */
  final def fireUpdateEvent(spectrumResult: SpectrumResult): Unit = {
    logger.debug(s"\t=>\tnotify all listener that the spectrum ${spectrumResult.getMonaId} has been updated")

    if (eventScheduler != null) {
      eventScheduler.scheduleEventProcessing(Event[SpectrumResult](spectrumResult, new Date, Event.UPDATE))
    }
  }

  final def fireSyncEvent(spectrumResult: SpectrumResult): Unit = {
    logger.debug(s"\t=>\tnotify all listener that the spectrum ${spectrumResult.getMonaId} has been scheduled for synchronization")

    if (eventScheduler != null) {
      eventScheduler.scheduleEventProcessing(Event[SpectrumResult](spectrumResult, new Date, Event.SYNC))
    }
  }

  /**
   * updates the provided spectrum
   *
   * @param spectrum
   * @return
   */
  @CacheEvict(value = Array("spectra"))
  final def update(spectrum: SpectrumResult): SpectrumResult = {
    //val serialized = objectMapper.writeValueAsString(spectrum);
    val result = spectrumResultRepository.save(spectrum)
    //val result = spectrumResultRepository.save(spectrum.copy(lastUpdated = new Date()).toString)
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
  final def save[S <: SpectrumResult](entity: S): S = {
    if(entity.getMonaId == null || entity.getMonaId == "") {
      val nextMonaId = sequenceService.getNextMoNAID
      entity.setMonaId(nextMonaId)
      entity.getSpectrum.setId(nextMonaId)
    }
    //val transformedEntity = entity.copy(
    //  monaId = Option(entity.getMonaId).getOrElse(sequenceService.getNextMoNAID),
      //dateCreated = Option(entity.dateCreated).getOrElse(new Date),
      //lastUpdated = new Date
    //)

    val result = spectrumResultRepository.save(entity)
    fireAddEvent(result)
    result
  }

  /**
   * updates the given spectra
   *
   * @param spectra
   */
  def update(spectra: lang.Iterable[SpectrumResult]): Unit = spectra.asScala.foreach(update)

  /**
   * removes the spectrum from the repository
   *
   * @param spectrum
   * @return
   */
  @CacheEvict(value = Array("spectra"))
  final def delete(spectrum: SpectrumResult): Unit = {
    spectrumResultRepository.delete(spectrum)
    fireDeleteEvent(spectrum)
  }

  @CacheEvict(value = Array("spectra"))
  final def deleteSparse(spectrum: SparseSearchTable): Unit = {
    spectrumResultRepository.deleteByMonaId(spectrum.getMonaId)
    fireDeleteEvent(spectrum)
  }


  /**
   * retrieves the spectrum from the repository
   *
   * @param id
   * @return
   */
  @Cacheable(value = Array("spectra"))
  def findByMonaId(id: String): SpectrumResult = spectrumResultRepository.findByMonaId(id)

  /**
   *
   * @param request
   * @return
   */
  private def findDataForQuery(rsqlQuery: String, request: Pageable): Page[SpectrumResult] = {
    logger.debug(s"executing query: \n$rsqlQuery\n")
    val rootNode: Node = new RSQLParser(operators).parse(rsqlQuery)
    val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
    val rez: java.util.List[String] = searchTableRepository.getMonaIdsFromResult(spec, classOf[SparseSearchTable], request)
    spectrumResultRepository.findAllByMonaIdIn(rez, PageRequest.of(0, request.getPageSize))
  }

  private def findDataForEmptyQuery(request: Pageable): Page[SpectrumResult] = {
    logger.debug(s"executing empty query to findAll")
    spectrumResultRepository.findAll(request)
  }

  /**
   * find all data without a query
   *
   * @return
   */
  def findAll(): lang.Iterable[SpectrumResult] = {
    new DynamicIterable[SpectrumResult, String]("", fetchSize) {

      /**
       * loads more data from the server for the given query
       */
      override def fetchMoreData(query: String, pageable: Pageable): Page[SpectrumResult] = findDataForEmptyQuery(pageable)
    }

  }

  /**
   * fires a synchronization event, so that system updates all it's clients. Be aware that this is very expensive!
   */
  def forceSynchronization(): Unit = findAll().asScala.foreach(fireSyncEvent)
  //def forceSynchronization(): Unit = findAll().iterator().asScala.foreach(fireSyncEvent)


  /**
   * queries for all the spectra matching this query
   *
   * @param rsqlQuery
   * @param textQuery
   * @return
   */
  @Cacheable(value = Array("spectra"))
  def findAll(rsqlQuery: String): lang.Iterable[SpectrumResult] = {

    /**
     * generates a new dynamic fetchable
     */
    val test = new DynamicIterable[SpectrumResult, String](rsqlQuery, fetchSize) {

      /**
       * loads more data from the server for the given query
       */
      override def fetchMoreData(query: String, pageable: Pageable): Page[SpectrumResult] = findDataForQuery(query, pageable)
    }
    logger.info(s"${test.asScala.size}")
    test.asScala.foreach{
      x =>
        logger.info(s"${x.getMonaId}")
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
  def findAll(query: String, pageable: Pageable): Page[SpectrumResult] = findDataForQuery(query, pageable)

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
    val rootNode: Node = new RSQLParser(operators).parse(query)
    val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
    val count: Long = searchTableRepository.countAll(spec, classOf[SparseSearchTable])
    count
  }

  @CacheEvict(value = Array("spectra"), allEntries = true)
  def deleteSpectraByIdIn(ids: java.util.List[String]): Unit = {
    spectrumResultRepository.findAllByMonaIdIn(ids).asScala.foreach(delete)
  }

  @CacheEvict(value = Array("spectra"), allEntries = true)
  def deleteSpectraByQuery(rsqlQuery: String): Unit = {
    val rootNode: Node = new RSQLParser(operators).parse(rsqlQuery)
    val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
    searchTableRepository.findAllWithoutPagination(spec, classOf[SparseSearchTable]).asScala.foreach(deleteSparse)
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
  def findAll(ids: java.util.List[String]): java.util.List[SpectrumResult] = spectrumResultRepository.findAllByMonaIdIn(ids)

  /**
   * checks if the given id exist in the database
   *
   * @param id
   * @return
   */
  @Cacheable(value = Array("spectra"))
  def existsById(id: String): Boolean = spectrumResultRepository.existsByMonaId(id)

  /**
   * finds all data with sorting.
   *
   * @param sort
   * @return
   */
  def findAll(sort: Sort): List[SpectrumResult] = spectrumResultRepository.findAll(sort)

  /**
   * finds all with pagination
   *
   * @param pageable
   * @return
   */
  def findAll(pageable: Pageable): Page[SpectrumResult] = spectrumResultRepository.findAll(pageable)

  @Cacheable(value = Array("spectra"))
  def existsById(id: SpectrumResultId): Boolean = spectrumResultRepository.existsById(id)

  @CacheEvict(value = Array("spectra"))
  def deleteById(id: String): Unit = spectrumResultRepository.deleteByMonaId(id)

  @CacheEvict(value = Array("spectra"), allEntries = true)
  def deleteAll(entities: lang.Iterable[_ <: SpectrumResult]): Unit = spectrumResultRepository.deleteAll(entities)

  //def saveAll(spectra: List[SpectrumResult]): Unit = spectrumResultRepository.saveAll(spectra)

  def saveAll[S <: SpectrumResult](entities: lang.Iterable[S]): lang.Iterable[S] = {
    entities.asScala.collect {
      case s: S => save(s)
    }.asJava
  }
}
