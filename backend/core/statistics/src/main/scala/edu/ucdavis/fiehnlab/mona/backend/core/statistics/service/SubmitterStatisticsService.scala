package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.SubmitterStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.SubmitterStatistics
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

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
      project("firstName", "lastName", "institution", "count")
        .and(ConditionalOperators.ifNull("score").`then`(0)).as("score"),

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
    results.foreach(submitterStatisticsRepository.save)
  }
}
