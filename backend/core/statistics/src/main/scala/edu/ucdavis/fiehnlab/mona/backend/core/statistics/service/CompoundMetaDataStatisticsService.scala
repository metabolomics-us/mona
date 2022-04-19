package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service
import com.mongodb.{BasicDBObject, DBObject}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.CompoundMetaDataStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{CompoundMetaDataStatistics, CompoundMetaDataStatisticsSummary}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.aggregation.{AggregationOperation, AggregationOperationContext}
import org.springframework.data.mongodb.core.query.{Criteria, Query}
import org.springframework.stereotype.Service

import scala.jdk.CollectionConverters._

@Service
class CompoundMetaDataStatisticsService {

  @Autowired
  private val mongoOperations: MongoOperations = null

  @Autowired
  @Qualifier("compoundMetadataStatisticsMongoRepository")
  private val compoundMetaDataStatisticsRepository: CompoundMetaDataStatisticsMongoRepository = null

  /**
   * Collect a list of unique metadata names
   *
   * @return
   */
  def compoundMetaDataNameAggregation(): Array[CompoundMetaDataStatisticsSummary] = {

    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      unwind("$compound"),
      unwind("$compound.metaData"),
      group("metaData.name").count().as("count")
    ).withOptions(newAggregationOptions().allowDiskUse(true).build())

    mongoOperations
      .aggregate(aggregationQuery, "SPECTRUM", classOf[CompoundMetaDataStatisticsSummary])
      .asScala
      .toArray
  }


  /**
   * Collect a list of unique metadata values and their respective counts for a given metadata name
   *
   * @param metaDataName name to query
   * @return
   */
  def compoundMetaDataAggregation(metaDataName: String, sliceCount: Int = 1000): CompoundMetaDataStatistics = {

    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      project("compound"),
      unwind("compound"),
      unwind("compound.metaData"),
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

    val results = mongoOperations.aggregate(aggregationQuery, classOf[Spectrum], classOf[CompoundMetaDataStatistics]).asScala

    if (results.isEmpty) {
      CompoundMetaDataStatistics(metaDataName, 0, Array())
    } else {
      results.head
    }
  }

  /**
   * Get all data in the metadata statistics repository
   *
   * @return
   */
  def getCompoundMetaDataStatistics: Iterable[CompoundMetaDataStatistics] = compoundMetaDataStatisticsRepository.findAll().asScala

  /**
   * Get data for the given metadata name from the metadata statistics repository
   *
   * @return
   */
  def getCompoundMetaDataStatistics(metaDataName: String): CompoundMetaDataStatistics = compoundMetaDataStatisticsRepository.findOne(metaDataName)

  /**
   * Get a list of unique metadata names from the metadata statistics repository
   *
   */
  def getCompoundMetaDataNames: Array[CompoundMetaDataStatisticsSummary] = {
    val query: Query = new Query()
    query.fields().exclude("values")

    mongoOperations.find(query, classOf[CompoundMetaDataStatisticsSummary], "STATISTICS_COMPOUND_METADATA").asScala.toArray
  }

  /**
   * Count the data in the metadata statistics repository
   *
   * @return
   */
  def countCompoundMetaDataStatistics: Long = compoundMetaDataStatisticsRepository.count()


  /**
   * Update the data in the metadata statistics repository
   *
   * @return
   */
  def updateCompoundMetaDataStatistics(sliceCount: Int = 1000): Unit = {
    val aggregationCompoundQuery = newAggregation(
      classOf[Spectrum],
      project("compound"),
      unwind("compound"),
      unwind("compound.metaData"),
      project().and("compound.metaData.name").as("name").and("compound.metaData.value").as("value"),
      group("name", "value").count().as("count"),
      project("name").and("grouped").nested(bind("value", "value").and("count", "count")),
      sort(Direction.DESC, "grouped.count").and(Direction.ASC, "grouped.value"),
      group("name").push("grouped").as("values").sum("grouped.count").as("count"),
      new AggregationOperation() {
        override def toDBObject(context: AggregationOperationContext): DBObject =
          context.getMappedObject(new BasicDBObject(
            "$project", new BasicDBObject(
              "values", new BasicDBObject("$slice", Array("$values", sliceCount))
            ).append("count", 1),
          ))
      }
    ).withOptions(newAggregationOptions().allowDiskUse(true).build())

    val results: Iterable[CompoundMetaDataStatistics] = mongoOperations
      .aggregate(aggregationCompoundQuery, classOf[Spectrum], classOf[CompoundMetaDataStatistics])
      .getMappedResults
      .asScala


    compoundMetaDataStatisticsRepository.deleteAll()
    results.foreach(compoundMetaDataStatisticsRepository.save(_))
  }
}
