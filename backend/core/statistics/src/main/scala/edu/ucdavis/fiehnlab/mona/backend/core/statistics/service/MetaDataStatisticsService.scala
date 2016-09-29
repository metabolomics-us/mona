package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.lang
import java.util.LinkedHashMap

import com.mongodb.DBObject
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.MetaDataStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, MetaDataValueCount}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.query.Criteria
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
  def metaDataNameAggregation(): Array[String] = {
    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      unwind("$metaData"),
      group("metaData.name")
    )

    mongoOperations
      .aggregate(aggregationQuery, "SPECTRUM", classOf[DBObject])
      .asScala
      .collect { case x: DBObject => x.get("_id").toString}
      .toArray
  }


  /**
    * Collect a list of unique metadata values and their respective counts for a given metadata name
    * @param metaDataName
    * @return
    */
  def metaDataAggregation(metaDataName: String): MetaDataStatistics = {
    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      project("metaData"),
      unwind("metaData"),
      `match`(Criteria.where("metaData.name").is(metaDataName)),
      project(bind("value", "metaData.value")),
      group("value").count().as("total"),
      project("total").and("value").previousOperation(),
      sort(Sort.Direction.DESC, "total")
    )

    val results = mongoOperations
      .aggregate(aggregationQuery, classOf[Spectrum], classOf[LinkedHashMap[String, Object]])
      .getMappedResults
      .asScala
      .map(x => MetaDataValueCount(x.get("value").toString, x.get("total").asInstanceOf[Int]))
      .toArray

    MetaDataStatistics(metaDataName, results)
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
    */
  def getMetaDataNames: Array[String] = mongoOperations.getCollection("STATISTICS_METADATA").distinct("_id").asScala.map(_.toString).toArray

  /**
    * Count the data in the metadata statistics repository
    * @return
    */
  def countMetaDataStatistics: Long = metaDataStatisticsRepository.count()

  /**
    * Update the data in the metadata statistics repository
    * @return
    */
  def updateMetaDataStatistics() =
  metaDataNameAggregation() foreach { x => metaDataStatisticsRepository.save(metaDataAggregation(x)) }
}
