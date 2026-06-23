package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{LibraryRepository, StatisticsTagRepository, TagsRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsTag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.{Propagation, Transactional}

import javax.persistence.EntityManager
import scala.collection.mutable.Map
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 9/27/16.
 * */
@Service
@Profile(Array("mona.persistence"))
class TagStatisticsService extends LazyLogging{

  @Autowired
  private val statisticsTagRepository: StatisticsTagRepository = null

  @Autowired
  private val tagsRepository: TagsRepository = null

  @Autowired
  private val libraryRepository: LibraryRepository = null

  @Autowired
  private val entityManager: EntityManager = null
  /**
   * Collect a list of unique tags with their respective counts
   *
   * @return
   * */
  @Transactional
  def updateTagStatistics(): String = {
    logger.info("Aggregating tag statistics now...")
    statisticsTagRepository.deleteAllInBatch()

    // Aggregate tag counts in the database, excluding library tags with no spectrum or compound
    // association, then combine the ruleBased variants of each text into a single total
    val tagsCounter: Map[String, Int] = Map()
    val tagsRuleBase: Map[String, Boolean] = Map()
    tagsRepository.aggregateTagCounts().asScala.foreach { aggregation =>
      val text = aggregation.getText
      tagsCounter(text) = tagsCounter.getOrElse(text, 0) + aggregation.getCount.toInt
      if (!tagsRuleBase.contains(text)) {
        tagsRuleBase(text) = aggregation.getRuleBased
      }
    }

    tagsCounter.foreach { case (text, count) =>
      val newStatisticTag = new StatisticsTag(text, tagsRuleBase(text), count, if (libraryRepository.existsByLibrary(text)) "library" else null)
      statisticsTagRepository.save(newStatisticTag)
    }
    val tagCount = tagsCounter.size
    entityManager.flush()
    entityManager.clear()
    logger.info(s"Tag statistics complete: $tagCount tags")
    "Tag Statistics Completed"
  }

    /**
     * Get all data in the tag statistics repository
     *
     * @return
     * */
    def getTagStatistics: Iterable[StatisticsTag] = statisticsTagRepository.findByOrderByCountDesc().asScala

    /**
     * Get all library tags in the tag statistics repository
     * */
    def getLibraryTagStatistics: Iterable[StatisticsTag] = statisticsTagRepository.findByCategoryOrderByCountDesc("library").asScala

    /**
     * Count the data in the tag statistics repository
     *
     * @return
     * */
    def countTagStatistics: Long = statisticsTagRepository.count()
}
