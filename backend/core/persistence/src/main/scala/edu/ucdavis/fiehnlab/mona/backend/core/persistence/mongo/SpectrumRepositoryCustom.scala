package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.data.domain.{Pageable, Page}
import org.springframework.data.mongodb.core.query.{BasicQuery, Query}

/**
  * Created by wohlgemuth on 2/26/16.
  */
trait SpectrumRepositoryCustom {

  /**
    * executes a custom query against the repository
    *
    * @param query
    * @return
    */
  def executeQuery(query: Query): java.util.List[Spectrum]

  /**
    * pagination based approach to query the system
    * @param query
    * @param pageable
    * @return
    */
  def executeQuery(query: Query, pageable: Pageable): Page[Spectrum]


  /**
    * simple wrapper, so we don't have to use a query object
    *
    * @param query
    * @return
    */
  def executeQuery(query: String): java.util.List[Spectrum] = {
    executeQuery(new BasicQuery(query))
  }

  /**
    *
    * @param query
    * @return
    */
  def executeQuery(query: String, pageable: Pageable): Page[Spectrum] = {
    executeQuery(new BasicQuery(query), pageable)
  }

  /**
    * executes a query against the system and returns the count
    * @param query
    * @return
    */
  def executeQueryCount(query: String) : Long = {
    executeQueryCount(new BasicQuery(query))
  }

  /**
    * executes query count against the system
    * @param query
    * @return
    */
  def executeQueryCount(query: Query) : Long
}
