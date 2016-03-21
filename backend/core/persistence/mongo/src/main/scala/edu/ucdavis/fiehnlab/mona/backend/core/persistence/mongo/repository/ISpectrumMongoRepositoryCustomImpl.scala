package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import com.github.rutledgepaulv.qbuilders.visitors.MongoVisitor
import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, PageImpl, Pageable}
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import scala.collection.JavaConverters._

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

  /**
    * saves our updaes a given element
    * implementation can be slow but should not cause
    * duplicated saves
    *
    * @param value
    * @return
    */
  override def saveOrUpdate(value: Spectrum): Unit = mongoOperations.save(value)

}
