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
  def executeQuery(query: Query): List[Spectrum]

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
  def executeQuery(query: String): List[Spectrum] = {
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
}
