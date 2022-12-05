package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{LibraryRepository, StatisticsTagRepository, TagsRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsTag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.collection.mutable.Map
import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters.StreamHasToScala

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

  def updateTagStatisticsHelper(): (Map[String, Int], Map[String, Boolean]) = {
    val tagsCounter: Map[String, Int] = Map()
    val tagsRuleBase: Map[String, Boolean] = Map()
    var counter = 0
    tagsRepository.streamAllBy().toScala(Iterator).foreach { tag =>
      //Exclude spectrum.library associated tags since they are already included in the spectrum.tags object
      if (tag.getSpectrum == null && tag.getCompound == null) {
        logger.debug(s"Don't count library tags as count as duplicates")
      } else {
        if (tagsCounter.contains(tag.getText)) {
          tagsCounter(tag.getText) += 1
        } else {
          tagsCounter(tag.getText) = 1
          tagsRuleBase(tag.getText) = tag.getRuleBased
        }
      }
      counter += 1

      if (counter % 100000 == 0) {
        logger.info(s"\tCompleted Tag Object #${counter}")
      }
    }
    (tagsCounter, tagsRuleBase)
  }
  /**
   * Collect a list of unique tags with their respective counts
   *
   * @return
   * */
  @Transactional(propagation =  org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def updateTagStatistics(): String = {
    statisticsTagRepository.deleteAll()

    val (tagsCounter, tagsRuleBase) = updateTagStatisticsHelper()

    tagsCounter.foreach { case (key, value) =>
      val newStatisticTag = new StatisticsTag(key, tagsRuleBase(key), value, if (libraryRepository.existsByTag_Text(key)) "library" else null)
      statisticsTagRepository.save(newStatisticTag)
    }
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
