package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository

import java.util

import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.rsql.{Context, CustomElastic1SearchVisitor}
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.index.query._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, PageRequest, Pageable}
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query._

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
  * Created by wohlg_000 on 3/3/2016.
  */
class ISpectrumElasticRepositoryCustomImpl extends SpectrumElasticRepositoryCustom with LazyLogging {

  @Autowired
  val elasticsearchTemplate: ElasticsearchTemplate = null

  /**
    * Uses the ElasticSearch scroll api to retrieve all matches for the given query
    * @param query
    * @return
    */
  override def nativeQuery(query: QueryBuilder): util.List[Spectrum] = {
    val search = getSearch(query)
    search.setPageable(new PageRequest(0, 25))

    val scrollId: String = elasticsearchTemplate.scan(search, 60000, false)
    val result: ArrayBuffer[Spectrum] = ArrayBuffer[Spectrum]()
    var hasRecords = true

    while(hasRecords) {
      val page: Page[Spectrum] = elasticsearchTemplate.scroll(scrollId, 60000, classOf[Spectrum])

      if (page.hasContent) {
        result ++= page.asScala
      } else {
        hasRecords = false
      }
    }

    result.asJava
  }

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
    val qb: QueryBuilder = condition.query(new CustomElastic1SearchVisitor(), new Context())
    qb
  }

  /**
    * executes a query against the system and returns the count
    *
    * @param query
    * @return
    */
  override def nativeQueryCount(query: QueryBuilder): Long = elasticsearchTemplate.count(getSearch(query), classOf[Spectrum])

  def getSearch(queryBuilder: QueryBuilder): SearchQuery = {
    val query = new NativeSearchQueryBuilder()
      .withQuery(queryBuilder)
      .build()

    logger.info(s"query: ${query.getQuery}")
    query
  }

  /**
    * saves our updates a given element
    * implementation can be slow but should not cause
    * duplicated saves
    *
    * @param value
    * @return
    */
  override def saveOrUpdate(value: Spectrum): Unit = {
    assert(value.id != null)
    elasticsearchTemplate.index(new IndexQueryBuilder().withId(value.id).withObject(value).build())
    elasticsearchTemplate.refresh(classOf[Spectrum])
  }

  /**
    * converts the text query string to a Query Object
    *
    * @param query
    * @return
    */
  override def buildFullTextQuery(query: String): QueryBuilder = {
    // Generate the following query which searches for exact match and then partial match,
    // boosting the score of the exact match:
    //    {
    //      "query": {
    //        "bool": {
    //          "should": [
    //            {
    //              "query_string": {
    //                "default_field": "_all",
    //                "query": "QUERY_STRING",
    //                "boost": 10
    //              }
    //            },
    //            {
    //              "query_string": {
    //                "default_field": "_all",
    //                "query": "*QUERY_STRING*",
    //                "rewrite": "scoring_boolean"
    //              }
    //            }
    //          ]
    //        }
    //      }
    //    }

    boolQuery()
      .should(queryStringQuery(query).defaultField("_all").boost(10))
      .should(queryStringQuery(s"*$query*").defaultField("_all").rewrite("scoring_boolean"))
  }

  /**
    * build a combined RSQL + full text query
    *
    * @param rsqlQueryString
    * @param textQueryString
    * @return
    */
  def buildQuery(rsqlQueryString: String, textQueryString: String): QueryBuilder = {
    if (textQueryString != null && textQueryString.nonEmpty) {
      if (rsqlQueryString != null && rsqlQueryString.nonEmpty) {
        boolQuery()
          .must(buildRSQLQuery(rsqlQueryString))
          .must(buildFullTextQuery(textQueryString))
      } else {
        buildFullTextQuery(textQueryString)
      }
    } else {
      buildRSQLQuery(rsqlQueryString)
    }
  }
}
