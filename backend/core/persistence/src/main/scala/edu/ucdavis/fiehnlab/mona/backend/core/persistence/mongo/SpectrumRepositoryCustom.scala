package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.data.domain.{Pageable, Page}
import org.springframework.data.mongodb.core.query.{BasicQuery, Query}

/**
  * Created by wohlgemuth on 2/26/16.
  */
trait SpectrumRepositoryCustom {
  /**
    * simple wrapper, so we don't have to use a query object
    *
    * @param query
    * @return
    */
  def executeQuery(query: String): java.util.List[Spectrum]
  /**
    *
    * @param query
    * @return
    */
  def executeQuery(query: String, pageable: Pageable): Page[Spectrum]
  /**
    * executes a query against the system and returns the count
    * @param query
    * @return
    */
  def executeQueryCount(query: String) : Long
}
