package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.MetaDataStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, MetaDataStatisticsSummary}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
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
    *
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
    *
    * Equivilent to:
    *   db.getCollection('SPECTRUM').aggregate([
    *     {$project: {metaData: 1}},
    *     {$unwind: "$metaData"},
    *     {$match: {"metaData.name": metaDataName}},
    *     {$project: {name: "$metaData.name", value: "$metaData.value"}},
    *     {$group: {_id: {name: "$name", value: "$value"}, count: {$sum: 1}}},
    *     {$sort: {count: -1}},
    *     {$group: {_id: "$_id.name", count: {$sum: 1}, values: {$push: {value: "$_id.value", count: "$count"}}}},
    *     {$sort: {count: -1}},
    *     {$project: {count: 1, values: {$slice: ["$values", sliceCount]}}}
    *   ])
    *
    * @param sliceCount
    * @param metaDataName name to query
    * @return
    */
  def metaDataAggregation(metaDataName: String, sliceCount: Int = 1000): MetaDataStatistics = {

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
      project("count").and("values").slice(sliceCount).as("values")
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
    *
    * @return
    */
  def getMetaDataStatistics: Iterable[MetaDataStatistics] = metaDataStatisticsRepository.findAll().asScala

  /**
    * Get data for the given metadata name from the metadata statistics repository
    *
    * @return
    */
  def getMetaDataStatistics(metaDataName: String): MetaDataStatistics = metaDataStatisticsRepository.findById(metaDataName).orElseGet(null)

  /**
    * Get a list of unique metadata names from the metadata statistics repository
    *
    */
  def getMetaDataNames: Array[MetaDataStatisticsSummary] = {
    val query: Query = new Query()
    query.fields().exclude("values")

    mongoOperations.find(query, classOf[MetaDataStatisticsSummary], "STATISTICS_METADATA").asScala.toArray
  }

  /**
    * Count the data in the metadata statistics repository
    *
    * @return
    */
  def countMetaDataStatistics: Long = metaDataStatisticsRepository.count()


  /**
    * Update the data in the metadata statistics repository
    *
    * Equivilent to:
    *   db.getCollection('SPECTRUM').aggregate([
    *     {$project: {metaData: 1}},
    *     {$unwind: "$metaData"},
    *     {$project: {name: "$metaData.name", value: "$metaData.value"}},
    *     {$group: {_id: {name: "$name", value: "$value"}, count: {$sum: 1}}},
    *     {$sort: {count: -1}},
    *     {$group: {_id: "$_id.name", count: {$sum: 1}, values: {$push: {value: "$_id.value", count: "$count"}}}},
    *     {$sort: {count: -1}},
    *     {$project: {count: 1, values: {$slice: ["$values", sliceCount]}}}
    *   ])
    *
    * @param sliceCount
    * @return
    */
  def updateMetaDataStatistics(sliceCount: Int = 1000): Unit = {

    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      project("metaData"),
      unwind("metaData"),
      project().and("metaData.name").as("name").and("metaData.value").as("value"),
      group("name", "value").count().as("count"),
      project("name").and("grouped").nested(bind("value", "value").and("count", "count")),
      sort(Direction.DESC, "grouped.count").and(Direction.ASC, "grouped.value"),
      group("name").push("grouped").as("values").sum("grouped.count").as("count"),
      project("count").and("values").slice(sliceCount).as("values")
    ).withOptions(newAggregationOptions().allowDiskUse(true).build())

    val results: Iterable[MetaDataStatistics] = mongoOperations
      .aggregate(aggregationQuery, classOf[Spectrum], classOf[MetaDataStatistics])
      .getMappedResults
      .asScala

    metaDataStatisticsRepository.deleteAll()
    results.foreach(metaDataStatisticsRepository.save)
  }
}
