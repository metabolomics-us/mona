package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.{lang, util}

import com.mongodb.{BasicDBObject, DBObject}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.MetaDataStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, MetaDataStatisticsSummary, MetaDataValueCount}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.aggregation.{AggregationOperation, AggregationOperationContext}
import org.springframework.data.mongodb.core.query.{Criteria, Query}
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._


/**
  * Created by sajjan on 9/27/16.
  */
@Service
class MetaDataStatisticsService {

  @Autowired
  private val mongoOperations: MongoOperations = null

  @Autowired
  @Qualifier("metadataStatisticsMongoRepository")
  private val metaDataStatisticsRepository: MetaDataStatisticsMongoRepository = null


  /**
    * Collect a list of unique metadata names
    * @return
    */
  def metaDataNameAggregation(): Array[MetaDataStatisticsSummary] = {

    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      unwind("$metaData"),
      group("metaData.name").count().as("count")
    ).withOptions(newAggregationOptions().allowDiskUse(true).build())

    mongoOperations
      .aggregate(aggregationQuery, "SPECTRUM", classOf[MetaDataStatisticsSummary])
      .asScala
      .toArray
  }


  /**
    * Collect a list of unique metadata values and their respective counts for a given metadata name
    * @param metaDataName name to query
    * @return
    */
  def metaDataAggregation(metaDataName: String, sliceCount: Int = 100): MetaDataStatistics = {

    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      project("metaData"),
      unwind("metaData"),
      `match`(Criteria.where("metaData.name").is(metaDataName)),
      project().and("metaData.name").as("name").and("metaData.value").as("value"),
      group("name", "value").count().as("count"),
      project("name").and("grouped").nested(bind("value", "value").and("count", "count")),
      sort(Direction.DESC, "grouped.count").and(Direction.ASC, "grouped.value"),
      group("name").push("grouped").as("values").sum("grouped.count").as("count"),

      // Needed to use the slice operation - can be removed when upgrading to in spring-data-mongodb 1.10.0.RELEASE
      new AggregationOperation() {
        override def toDBObject(context: AggregationOperationContext): DBObject =
          context.getMappedObject(new BasicDBObject(
            "$project", new BasicDBObject(
              "values", new BasicDBObject("$slice", Array("$values", sliceCount))
            ).append("count", 1)
          ))
      }
    ).withOptions(newAggregationOptions().allowDiskUse(true).build())

    val results = mongoOperations.aggregate(aggregationQuery, classOf[Spectrum], classOf[MetaDataStatistics]).asScala

    if (results.isEmpty) {
      MetaDataStatistics(metaDataName, 0, Array())
    } else {
      results.head
    }
  }

  /**
    * Get all data in the metadata statistics repository
    * @return
    */
  def getMetaDataStatistics: lang.Iterable[MetaDataStatistics] = metaDataStatisticsRepository.findAll

  /**
    * Get data for the given metadata name from the metadata statistics repository
    * @return
    */
  def getMetaDataStatistics(metaDataName: String): MetaDataStatistics = metaDataStatisticsRepository.findOne(metaDataName)

  /**
    * Get a list of unique metadata names from the metadata statistics repository
    *
    */
  def getMetaDataNames: Array[MetaDataStatisticsSummary] = {
    val query: Query = new Query()
    query.fields().exclude("values")

    mongoOperations.find(query, classOf[MetaDataStatisticsSummary], "STATISTICS_METADATA")
      .asScala
      .toArray
  }

  /**
    * Count the data in the metadata statistics repository
    * @return
    */
  def countMetaDataStatistics: Long = metaDataStatisticsRepository.count()


  /**
    * Update the data in the metadata statistics repository
    * @return
    */
  def updateMetaDataStatistics(sliceCount: Int = 100): Unit = {

    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      project("metaData"),
      unwind("metaData"),
      project().and("metaData.name").as("name").and("metaData.value").as("value"),
      group("name", "value").count().as("count"),
      project("name").and("grouped").nested(bind("value", "value").and("count", "count")),
      sort(Direction.DESC, "grouped.count").and(Direction.ASC, "grouped.value"),
      group("name").push("grouped").as("values").sum("grouped.count").as("count"),

      // Needed to use the slice operation - can be removed when upgrading to in spring-data-mongodb 1.10.0.RELEASE
      new AggregationOperation() {
        override def toDBObject(context: AggregationOperationContext): DBObject =
          context.getMappedObject(new BasicDBObject(
            "$project", new BasicDBObject(
              "values", new BasicDBObject("$slice", Array("$values", sliceCount))
            ).append("count", 1)
          ))
      }
    ).withOptions(newAggregationOptions().allowDiskUse(true).build())

    val results: Iterable[MetaDataStatistics] = mongoOperations
      .aggregate(aggregationQuery, classOf[Spectrum], classOf[MetaDataStatistics])
      .getMappedResults
      .asScala

    metaDataStatisticsRepository.deleteAll()
    results.foreach(metaDataStatisticsRepository.save(_))
  }
}
