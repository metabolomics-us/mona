package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.lang
import java.util.LinkedHashMap

import com.mongodb.DBObject
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.{MetaDataStatisticsMongoRepository, TagStatisticsMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, TagStatistics}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 8/2/16.
  */
@Service
class StatisticsService {

  @Autowired
  val mongoOperations: MongoOperations = null

  @Autowired
  @Qualifier("metadataStatisticsMongoRepository")
  val metaDataStatisticsRepository: MetaDataStatisticsMongoRepository = null

  @Autowired
  @Qualifier("tagStatisticsMongoRepository")
  val tagStatisticsRepository: TagStatisticsMongoRepository = null


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

  def metaDataAggregation(): Array[MetaDataStatistics] = {
    metaDataNameAggregation().collect {
      case metaDataName: String =>
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
          .map(x => (x.get("value").toString, x.get("total").asInstanceOf[Int]))
          .toArray

        MetaDataStatistics(metaDataName, results)
    }
  }

  def getMetaDataStatistics: lang.Iterable[MetaDataStatistics] = metaDataStatisticsRepository.findAll

  def countMetaDataStatistics: Long = metaDataStatisticsRepository.count()

  def updateMetaDataStatistics() = metaDataAggregation().foreach(metaDataStatisticsRepository.save(_))


  def tagAggregation(): Array[TagStatistics] = {
    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      unwind("$tags"),
      project(bind("text", "tags.text")),
      group("text").count().as("total"),
      project("total").and("value").previousOperation(),
      sort(Sort.Direction.DESC, "total")
    )

    mongoOperations
      .aggregate(aggregationQuery, "SPECTRUM", classOf[DBObject])
      .asScala
      .map(x => TagStatistics(x.get("value").toString, x.get("total").asInstanceOf[Int]))
      .toArray
  }

  def getTagStatistics: lang.Iterable[TagStatistics] = tagStatisticsRepository.findAll

  def countTagStatistics: Long = tagStatisticsRepository.count()

  def updateTagStatistics() = tagAggregation().foreach(tagStatisticsRepository.save(_))


  def updateStatistics() = {
    updateTagStatistics()
    updateMetaDataStatistics()
  }
}
