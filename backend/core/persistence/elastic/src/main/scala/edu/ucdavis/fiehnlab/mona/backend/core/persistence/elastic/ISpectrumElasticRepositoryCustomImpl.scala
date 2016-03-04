package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic

import java.util

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.{NativeSearchQueryBuilder, NativeSearchQuery}

/**
  * Created by wohlg_000 on 3/3/2016.
  */
class ISpectrumElasticRepositoryCustomImpl extends SpectrumElasticRepositoryCustom{

  @Autowired
  val elasticsearchTemplate:ElasticsearchTemplate = null

  /**
    * simple wrapper, so we don't have to use a query object
    *
    * @param query
    * @return
    */
  override def executeQuery(query: String): util.List[Spectrum] = ???
  /**
    * executes a query against the system and returns the count
    *
    * @param query
    * @return
    */
  override def executeQueryCount(query: String): Long = ???

  /**
    *
    * @param query
    * @return
    */
  override def executeQuery(query: String, pageable: Pageable): Page[Spectrum] = ???
}
