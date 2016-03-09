package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql

import java.util

import com.github.rutledgepaulv.qbuilders.visitors.MongoVisitor
import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.mongodb.core.query.{BasicQuery, Query}
import org.springframework.data.repository.NoRepositoryBean

/**
  * Created by wohlgemuth on 3/9/16.
  */

@NoRepositoryBean
trait RSQLRepositoryCustom[T] {


  /**
    *
    * @param query
    * @return
    */
  def nativeQuery(query: Query): java.util.List[T]

  /**
    *
    * @param query
    * @return
    */
  def nativeQuery(query: Query, pageable: Pageable): Page[T]

  /**
    * @param query
    * @return
    */
  def nativeQueryCount(query: Query): Long

  /**
    * @param query
    * @return
    */
  def nativeQuery(query: String): util.List[T] = nativeQuery(new BasicQuery(query))

  /**
    * executes a query against the system and returns the count
    *
    * @param query
    * @return
    */
  def nativeQueryCount(query: String): Long = nativeQueryCount(new BasicQuery(query))

  /**
    *
    * @param query
    * @returngit pull
    *
    */
  def nativeQuery(query: String, pageable: Pageable): Page[T] = nativeQuery(new BasicQuery(query), pageable)


  /**
    * executes an RSQL query
    *
    * @param query
    * @return
    */
  def rsqlQuery(query: String): java.util.List[T] = nativeQuery(buildRSQLQuery(query))


  /**
    *
    * @param query
    * @return
    */
  def rsqlQuery(query: String, pageable: Pageable): Page[T] = nativeQuery(buildRSQLQuery(query), pageable)


  /**
    * executes the given query and returns it's count
    *
    * @param query
    * @return
    */
  def rsqlQueryCount(query: String): Long = nativeQueryCount(buildRSQLQuery(query))

  /**
    * converts the RSQL String for us to a Query Object
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
}