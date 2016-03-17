package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository

import java.util

import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.rsql.{Context, CustomElastic1SearchVisitor}
import org.elasticsearch.action.delete.{DeleteRequestBuilder, DeleteRequest}
import org.elasticsearch.client.Client
import org.elasticsearch.index.query._
import org.elasticsearch.search.SearchHit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, PageRequest, Pageable}
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query._
import org.elasticsearch.index.query.QueryBuilders._
/**
  * Created by wohlg_000 on 3/3/2016.
  */
class ISpectrumElasticRepositoryCustomImpl extends SpectrumElasticRepositoryCustom with LazyLogging {

  @Autowired
  val elasticsearchTemplate: ElasticsearchTemplate = null

  @Autowired
  val elasticClient: Client = null

  /**
    * @param query
    * @return
    */
  override def nativeQuery(query: QueryBuilder): util.List[Spectrum] = elasticsearchTemplate.queryForList(getSearch(query), classOf[Spectrum])

  /**
    *
    * @param query
    * @return
    *
    */
  override def nativeQuery(query: QueryBuilder, pageable: Pageable): Page[Spectrum] = {
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
  override def buildRSQLQuery(query: String): QueryBuilder = {
    val pipeline = QueryConversionPipeline.defaultPipeline()
    val condition = pipeline.apply(query, classOf[Spectrum])
    val qb: QueryBuilder = condition.query(new CustomElastic1SearchVisitor(),new Context())
    qb
  }

  /**
    * executes a query against the system and returns the count
    *
    * @param query
    * @return
    */
  override def nativeQueryCount(query: QueryBuilder): Long = elasticsearchTemplate.count(getSearch(query),classOf[Spectrum])

  def getSearch(queryBuilder: QueryBuilder): SearchQuery = {
    //uggly but best solution I found so far. If we do it without pagination request, spring will always limit it to 10 results.
    val query = new NativeSearchQueryBuilder().withQuery(queryBuilder).withPageable(new PageRequest(0,1000000)).build()
    query
  }

  /**
    * saves our updaes a given element
    * implementation can be slow but should not cause
    * duplicated saves
    *
    * @param value
    * @return
    */
  override def saveOrUpdate(value: Spectrum): Unit = {
    elasticsearchTemplate.index(new IndexQueryBuilder().withObject(value).build())
  }

  override def deleteByMe(value:Spectrum): Unit = {

    val bulk = elasticClient.prepareBulk().setRefresh(true)

    val hits: Array[SearchHit] = elasticClient.prepareSearch("spectrum").setTypes("spectrum").setQuery(filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.termFilter("id", value.id))).setSize(1).execute().actionGet().getHits.getHits

    if (hits.length > 0) {
      hits.foreach { hit =>
        logger.info(s"deleting object with id: ${hit.id}")
        val deleteRequest = new DeleteRequest("spectrum", "spectrum", hit.getId)
        deleteRequest.refresh(true)
        bulk.add(deleteRequest)

      }

      bulk.execute().actionGet()

      val count = elasticClient.prepareCount("spectrum").setQuery(matchAllQuery()).execute().actionGet().getCount
      logger.error("my stupid count is: " + count)
      deleteByMe(value)

    }

  }
}
