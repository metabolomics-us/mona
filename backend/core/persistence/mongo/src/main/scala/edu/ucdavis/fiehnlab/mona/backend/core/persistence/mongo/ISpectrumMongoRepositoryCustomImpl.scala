package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import java.util

import com.github.rutledgepaulv.qbuilders.visitors.MongoVisitor
import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import jdk.nashorn.internal.ir.visitor.NodeVisitor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Pageable, PageImpl, Page}
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.{Criteria, BasicQuery, Query}
import org.springframework.stereotype.Repository
import scala.collection.JavaConverters._
import scala.reflect.ClassTag

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
  override def nativeQueryCount(query: Query): Long = mongoOperations.count(query, classOf[Spectrum])

  /**
    * converts the RSQL String for us to a Query Object
    *
    * @param query
    * @return
    */
  override def buildRSQLQuery(query: String): Query = {
    val pipeline = QueryConversionPipeline.defaultPipeline()
    val condition = pipeline.apply(query, classOf[Spectrum])
    val criteria = condition.query(new MongoVisitor())
    val toExecute = new Query()
    toExecute.addCriteria(criteria)

    toExecute
  }
}
