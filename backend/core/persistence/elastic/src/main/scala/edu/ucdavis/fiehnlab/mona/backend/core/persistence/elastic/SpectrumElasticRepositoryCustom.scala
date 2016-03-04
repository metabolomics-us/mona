package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.data.domain.{Page, Pageable}

/**
  * Created by wohlg_000 on 3/3/2016.
  */
trait SpectrumElasticRepositoryCustom {
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
    *
    * @param query
    * @return
    */
  def executeQueryCount(query: String) : Long

}
