package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, PageImpl, Pageable}
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.{Criteria, Query, TextCriteria, TextQuery}
import org.springframework.stereotype.Repository
import rsql.CustomMongoVisitor

/**
  * Created by wohlgemuth on 2/26/16.
  */

@Repository
class ISpectrumMongoRepositoryCustomImpl extends SpectrumMongoRepositoryCustom with LazyLogging {

  @Autowired
  val mongoOperations: MongoOperations = null

  /**
    * provide a query to receive a list of the provided  objects
    *
    * @param query
    * @return
    */
  def nativeQuery(query: Query): java.util.List[Spectrum] = {
    logger.info(s"query:\n\n${query.toString}\n\n")
    mongoOperations.find(query, classOf[Spectrum])
  }

  /**
    * pagination based approach to query the system
    *
    * @param query
    * @param pageable
    * @return
    */
  def nativeQuery(query: Query, pageable: Pageable): Page[Spectrum] = {
    val count = nativeQueryCount(query)

    query.`with`(pageable)

    val result: java.util.List[Spectrum] = mongoOperations.find(query, classOf[Spectrum])

    new PageImpl[Spectrum](result, pageable, count)
  }

  /**
    * @param query
    * @return
    */
  def nativeQueryCount(query: Query): Long = mongoOperations.count(query, classOf[Spectrum])


  /**
    * constructs MongoDB Criteria from RSQL query
    *
    * @param query
    * @return
    */
  private def buildRSQLCriteria(query: String): Criteria = {
    val pipeline = QueryConversionPipeline.defaultPipeline()
    val condition = pipeline.apply(query, classOf[Spectrum])
    condition.query(new CustomMongoVisitor())
  }

  /**
    * converts the RSQL String for us to a Query Object
    *
    * @param query
    * @return
    */
  override def buildRSQLQuery(query: String): Query = new Query(buildRSQLCriteria(query))

  /**
    * saves our updates a given element
    * implementation can be slow but should not cause
    * duplicated saves
    *
    * @param value
    * @return
    */
  override def saveOrUpdate(value: Spectrum): Unit = mongoOperations.save(value)

  /**
    * converts the text query string to a Query Object
    *
    * @param query
    * @return
    */
  def buildFullTextQuery(query: String): Query = TextQuery.queryText(TextCriteria.forDefaultLanguage.matching(query))

  /**
    * build a combined RSQL + full text query
    *
    * @param rsqlQueryString
    * @param textQueryString
    * @return
    */
  def buildQuery(rsqlQueryString: String, textQueryString: String): Query = {
    if (textQueryString != null && textQueryString.nonEmpty) {
      if (rsqlQueryString != null && rsqlQueryString.nonEmpty) {
        buildFullTextQuery(textQueryString)
          .addCriteria(buildRSQLCriteria(rsqlQueryString))
      } else {
        buildFullTextQuery(textQueryString)
      }
    } else {
      buildRSQLQuery(rsqlQueryString)
    }
  }
}
