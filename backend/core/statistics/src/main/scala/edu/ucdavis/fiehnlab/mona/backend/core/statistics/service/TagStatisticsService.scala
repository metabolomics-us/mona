package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.TagStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.TagStatistics
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
      project().and("tags.text").as("text").and("tags.ruleBased").as("ruleBased"),
      group("text", "ruleBased").count().as("count"),
      project("count").and("_id.text").as("text").and("_id.ruleBased").as("ruleBased"),
      sort(Sort.Direction.DESC, "count")
    ).withOptions(newAggregationOptions().allowDiskUse(true).build())

    mongoOperations
      .aggregate(aggregationQuery, "SPECTRUM", classOf[TagStatistics])
      .asScala.toArray
  }

  /**
    * Collect a list of library tags from the library sub-object and update the tag statistics collection
    * by marking entries with the "library" category
    * @return
    */
  def libraryTagsAggregation(): Array[TagStatistics] = {
    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      `match`(Criteria.where("library.tag.text").ne(null)),
      group("library.tag.text").count().as("count")
    )

    //clear out old libraries
    getLibraryTagStatistics.foreach{x:TagStatistics =>
      tagStatisticsRepository.delete(x.id)
    }

    mongoOperations
      .aggregate(aggregationQuery, "SPECTRUM", classOf[AggregationResult])
      .asScala
      .toArray
      .map { x =>
        val tagStatistics: Array[TagStatistics] = tagStatisticsRepository.findByText(x._id)

        if (tagStatistics.nonEmpty) {
          tagStatisticsRepository.save(tagStatistics.head.copy(category = "library"))
        } else {
          tagStatisticsRepository.save(TagStatistics(null, x._id, ruleBased = false, x.count.toInt, "library"))
        }
      }
  }

  /**
    * Get all data in the tag statistics repository
    * @return
    */
  def getTagStatistics: Iterable[TagStatistics] = tagStatisticsRepository.findAll().asScala

  /**
    * Get all library tags in the tag statistics repository
    */
  def getLibraryTagStatistics: Iterable[TagStatistics] = tagStatisticsRepository.findAllByCategory("library")

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
    // Tag aggregation
    val results: Array[TagStatistics] = tagAggregation()

    tagStatisticsRepository.deleteAll()
    results.foreach(tagStatisticsRepository.save(_))

    // Update tags by library information
    libraryTagsAggregation()
  }
}
