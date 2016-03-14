package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic

import java.util

import com.github.rutledgepaulv.qbuilders.visitors.{ElasticsearchVisitor, MongoVisitor}
import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.rsql.CustomElasticSearchVisitor
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder
import org.elasticsearch.index.query.{FilterBuilder, QueryBuilders, QueryBuilder}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{PageRequest, Page, Pageable}
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query._
import org.springframework.data.mongodb.core.query.Query

/**
  * Created by wohlg_000 on 3/3/2016.
  */
class ISpectrumElasticRepositoryCustomImpl extends SpectrumElasticRepositoryCustom with LazyLogging {

  @Autowired
  val elasticsearchTemplate: ElasticsearchTemplate = null

  /**
    * @param query
    * @return
    */
  override def nativeQuery(query: FilterBuilder): util.List[Spectrum] = elasticsearchTemplate.queryForList(getSearch(query), classOf[Spectrum])

  /**
    *
    * @param query
    * @return
    *
    */
  override def nativeQuery(query: FilterBuilder, pageable: Pageable): Page[Spectrum] = {
    val search = getSearch(query)
    search.setPageable(pageable)

    elasticsearchTemplate.queryForPage(search, classOf[Spectrum])
  }

  /**
    * converts the RSQL String for us to a Query Object
    *
    * @param query
    * @return
    */
  override def buildRSQLQuery(query: String): FilterBuilder = {
    val pipeline = QueryConversionPipeline.defaultPipeline()
    val condition = pipeline.apply(query, classOf[Spectrum])
    val qb: FilterBuilder = condition.query(new CustomElasticSearchVisitor())
    qb
  }

  /**
    * executes a query against the system and returns the count
    *
    * @param query
    * @return
    */
  override def nativeQueryCount(query: FilterBuilder): Long = elasticsearchTemplate.count(getSearch(query))

  def getSearch(queryFilter: FilterBuilder): SearchQuery = {
    val queryBuilder = QueryBuilders.matchAllQuery()

    //uggly but best solution I gound so far. If we do it without pagination request, spring will always limit it to 10 results.
    val query = new NativeSearchQueryBuilder().withQuery(queryBuilder).withFilter(queryFilter).withPageable(new PageRequest(0,1000000)).build()
    query
  }
}
