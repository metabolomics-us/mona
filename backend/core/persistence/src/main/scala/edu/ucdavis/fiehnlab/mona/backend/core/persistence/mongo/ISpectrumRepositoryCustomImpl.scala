package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import java.util

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.{Pageable, PageImpl, Page}
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.{BasicQuery, Query}
import org.springframework.stereotype.Repository
import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 2/26/16.
  */
@Repository
class ISpectrumRepositoryCustomImpl extends SpectrumRepositoryCustom {

  /**
    * simple wrapper, so we don't have to use a query object
    *
    * @param query
    * @return
    */
  override def executeQuery(query: String): util.List[Spectrum] = executeQuery(new BasicQuery(query))

  /**
    * executes a query against the system and returns the count
    *
    * @param query
    * @return
    */
  override def executeQueryCount(query: String): Long = executeQueryCount(new BasicQuery(query))

  /**
    *
    * @param query
    * @return
    */
  override def executeQuery(query: String, pageable: Pageable): Page[Spectrum] = executeQuery(new BasicQuery(query),pageable)

  @Autowired
  val mongoOperations: MongoOperations = null

  /**
    * provide a query to receive a list of the provided  objects
    *
    * @param query
    * @return
    */
  def executeQuery(query: Query): java.util.List[Spectrum] = {
    mongoOperations.find(query, classOf[Spectrum])
  }

  /**
    * pagination based approach to query the system
    *
    * @param query
    * @param pageable
    * @return
    */
  def executeQuery(query: Query, pageable: Pageable): Page[Spectrum] = {
    val count = executeQueryCount(query)

    query.`with`(pageable)

    val result: java.util.List[Spectrum] = mongoOperations.find(query, classOf[Spectrum])

    new PageImpl[Spectrum](result, pageable, count)
  }

  /**
    * executes query count against the system
    *
    * @param query
    * @return
    */
  def executeQueryCount(query: Query): Long = {
    mongoOperations.count(query, classOf[Spectrum])
  }
}
