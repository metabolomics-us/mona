package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import java.util

import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Pageable, PageImpl, Page}
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.{BasicQuery, Query}
import org.springframework.stereotype.Repository
import scala.collection.JavaConverters._
import com.github.rutledgepaulv.qbuilders.visitors.MongoVisitor

/**
  * Created by wohlgemuth on 2/26/16.
  */
@Repository
class ISpectrumMongoRepositoryCustomImpl extends SpectrumMongoRepositoryCustom with LazyLogging{

  /**
    * simple wrapper, so we don't have to use a query object
    *
    * @param query
    * @return
    */
  override def nativeQuery(query: String): util.List[Spectrum] = nativeQuery(new BasicQuery(query))

  /**
    * executes a query against the system and returns the count
    *
    * @param query
    * @return
    */
  override def nativeQueryCount(query: String): Long = executeQueryCount(new BasicQuery(query))

  /**
    *
    * @param query
    * @returngit pull
    *
    */
  override def nativeQuery(query: String, pageable: Pageable): Page[Spectrum] = nativeQuery(new BasicQuery(query), pageable)

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

  /**
    * executes an RSQL query
    *
    * @param query
    * @return
    */
  override def rsqlQuery(query: String): util.List[Spectrum] = {
    nativeQuery(buildRSQLQuery(query))
  }

  /**
    * builds a RSQL query object
    *
    * @param query
    * @return
    */
  def buildRSQLQuery(query: String): Query = {

    val pipeline = QueryConversionPipeline.defaultPipeline()
    val condition = pipeline.apply(query, classOf[Spectrum])
    val criteria = condition.query(new MongoVisitor())

    val toExecute = new Query()
    toExecute.addCriteria(criteria)

    toExecute
  }

  /**
    * executes the given query and returns it's count
    *
    * @param query
    * @return
    */
  override def rsqlQueryCount(query: String): Long = {
    executeQueryCount(buildRSQLQuery(query))
  }

  /**
    *
    * @param query
    * @return
    */
  override def rsqlQuery(query: String, pageable: Pageable): Page[Spectrum] = {
    nativeQuery(buildRSQLQuery(query), pageable)
  }
}
