package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.lang

import com.mongodb.DBObject
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.TagStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.TagStatistics
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 9/27/16.
  */
@Service
class TagStatisticsService {

  @Autowired
  private val mongoOperations: MongoOperations = null

  @Autowired
  @Qualifier("tagStatisticsMongoRepository")
  private val tagStatisticsRepository: TagStatisticsMongoRepository = null


  /**
    * Collect a list of unique tags with their respective counts
    * @return
    */
  def tagAggregation(): Array[TagStatistics] = {
    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      unwind("$tags"),
      project(bind("text", "tags.text")),
      group("text").count().as("total"),
      project("total").and("value").previousOperation(),
      sort(Sort.Direction.DESC, "total")
    ).withOptions(newAggregationOptions().allowDiskUse(true).build())

    mongoOperations
      .aggregate(aggregationQuery, "SPECTRUM", classOf[DBObject])
      .asScala
      .map(x => TagStatistics(x.get("value").toString, x.get("total").asInstanceOf[Int]))
      .toArray
  }

  /**
    * Get all data in the tag statistics repository
    * @return
    */
  def getTagStatistics: lang.Iterable[TagStatistics] = tagStatisticsRepository.findAll

  /**
    * Count the data in the tag statistics repository
    * @return
    */
  def countTagStatistics: Long = tagStatisticsRepository.count()

  /**
    * Update the data in the tag statistics repository
    * @return
    */
  def updateTagStatistics(): Unit = {
    val results: Array[TagStatistics] = tagAggregation()

    tagStatisticsRepository.deleteAll()
    results.foreach(tagStatisticsRepository.save(_))
  }
}
