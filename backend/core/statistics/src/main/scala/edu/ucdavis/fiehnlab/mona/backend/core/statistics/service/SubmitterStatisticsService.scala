package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.mongodb.{BasicDBObject, BasicDBObjectBuilder, DBObject}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.SubmitterStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.SubmitterStatistics
import org.bson.Document
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.aggregation.{AggregationOperation, AggregationOperationContext}
import org.springframework.stereotype.Service

import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 3/8/17.
  */
@Service
class SubmitterStatisticsService {

  @Autowired
  private val mongoOperations: MongoOperations = null

  @Autowired
  @Qualifier("submitterStatisticsMongoRepository")
  val submitterStatisticsRepository: SubmitterStatisticsMongoRepository = null


  def submitterAggregation(): Array[SubmitterStatistics] = {
    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      group("submitter.id")
        .first("submitter.firstName").as("firstName")
        .first("submitter.lastName").as("lastName")
        .first("submitter.institution").as("institution")
        .count().as("count")
        .avg("score.score").as("score"),

      // Needed to handle null average results to map to a number
      new AggregationOperation() {
        override def toDocument(context: AggregationOperationContext): Document =
          context.getMappedObject(new Document(
            "$project",
            BasicDBObjectBuilder.start()
              .add("firstName", 1)
              .add("lastName", 1)
              .add("institution", 1)
              .add("count", 1)
              .add("score", new Document("$ifNull", Array("$score", 0)))
              .get()
          ))
      },

      sort(Sort.Direction.DESC, "score")
    ).withOptions(newAggregationOptions().allowDiskUse(true).build())

    mongoOperations
      .aggregate(aggregationQuery, "SPECTRUM", classOf[SubmitterStatistics])
      .asScala.toArray
  }


  /**
    * Get all data in the submitter statistics repository
    *
    * @return
    */
  def getSubmitterStatistics: Iterable[SubmitterStatistics] = submitterStatisticsRepository.findAll().asScala


  /**
    * Update the data in the submitter statistics repository
    *
    * @return
    */
  def updateSubmitterStatistics(): Unit = {
    // Tag aggregation
    val results: Array[SubmitterStatistics] = submitterAggregation()

    submitterStatisticsRepository.deleteAll()
    results.foreach(submitterStatisticsRepository.save(_))
  }
}
