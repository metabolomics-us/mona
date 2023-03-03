package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.util.Date
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{CompoundRepository, MetaDataRepository, SpectrumRepository, SpectrumSubmitterRepository, StatisticsGlobalRepository, StatisticsTagRepository, TagsRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsGlobal
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.{Async, Scheduled}
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

import scala.collection.mutable.{ArrayBuffer, Map}
import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters.StreamHasToScala

/**
  * Created by sajjan on 8/2/16.
 * */
@Service
@Profile(Array("mona.persistence"))
class StatisticsService extends LazyLogging {
  @Autowired
  private val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  private val globalStatisticsRepository: StatisticsGlobalRepository = null

  @Autowired
  private val metaDataStatisticsService: MetaDataStatisticsService = null

  @Autowired
  private val metaDataRepository: MetaDataRepository = null

  @Autowired
  private val tagStatisticsService: TagStatisticsService = null

  @Autowired
  private val tagsRepository: TagsRepository = null

  @Autowired
  private val statisticsTagRepository: StatisticsTagRepository = null

  @Autowired
  val submitterStatisticsService: SubmitterStatisticsService = null

  @Autowired
  val spectraSubmittersRepository: SpectrumSubmitterRepository = null

  @Autowired
  val compoundClassStatisticsService: CompoundClassStatisticsService = null

  @Autowired
  private val compoundRepository: CompoundRepository = null

  @Autowired
  private val entityManager: EntityManager = null


  @Transactional
  def generateCompoundCount(): Long = {
    var counter = 0
    val inchiKeys: ArrayBuffer[String] = ArrayBuffer()
    compoundRepository.streamAllBy().toScala(Iterator).foreach { compound =>
      compound.getMetaData.asScala.foreach { metadata =>
        if (metadata.getName == "InChIKey") {
          inchiKeys.append(metadata.getValue.substring(0, 14))
        }
      }
      counter+=1
      entityManager.detach(compound)
      if (counter % 100000 == 0) {
        logger.info(s"\tCompleted Compound Count #${counter}")
      }
    }
    val finalCount = inchiKeys.distinct.length
    inchiKeys.clearAndShrink()
    entityManager.flush()
    entityManager.clear()
    finalCount
  }


  @Transactional
  def generateMetaDataCount(): Long = {
    val metaDataCounterMap: Map[String, Int] = Map()
    var counter = 0
    metaDataRepository.streamAllBy().toScala(Iterator).foreach{ metaData =>
      if(!metaDataCounterMap.contains(metaData.getName)) {
        metaDataCounterMap(metaData.getName) = 1
      }
      counter+=1
      entityManager.detach(metaData)
      if (counter % 100000 == 0) {
        logger.info(s"\tCompleted MetaData Count #${counter}")
        entityManager.flush()
        entityManager.clear()
      }
    }
    val finalCount = metaDataCounterMap.size.toLong
    metaDataCounterMap.clear()
    entityManager.flush()
    entityManager.clear()
    finalCount
  }


  @Transactional
  def generateTagCount(): Long = {
    val tagsCounter: Map[String, Int] = Map()
    var counter = 0
    tagsRepository.streamAllBy().toScala(Iterator).foreach { tag =>
      if(tag.getSpectrum == null && tag.getCompound == null) {
        logger.debug(s"Exclude Library Tag Duplicates")
      } else {
        if (!tagsCounter.contains(tag.getText)) {
          tagsCounter(tag.getText) = 1
        }
      }
      counter+=1
      entityManager.detach(tag)
      if (counter % 100000 == 0) {
        logger.info(s"\tCompleted Tag Count #${counter}")
      }
    }
    val finalCount = tagsCounter.size.toLong
    tagsCounter.clear()
    entityManager.flush()
    entityManager.clear()
    finalCount
  }


  @Transactional
  def generateSubmitterCount(): Long = {
    val submitterCounter: Map[String, Integer] = Map()
    var counter = 0
    spectraSubmittersRepository.streamAllBy().toScala(Iterator).foreach { submitter =>
      if (!submitterCounter.contains(submitter.getEmailAddress)) {
        submitterCounter(submitter.getEmailAddress) = 1
      }
      counter+=1
      entityManager.detach(submitter)
      if (counter % 10000 == 0) {
        logger.info(s"\tCompleted Submitter Count #${counter}")
      }
    }
    val finalCount = submitterCounter.size.toLong
    submitterCounter.clear()
    entityManager.flush()
    entityManager.clear()
    finalCount
  }
  /**
    * Update the data in the global statistics repository
    *
    * @return
    **/

  @Transactional
  def updateGlobalStatistics(): String = {
    globalStatisticsRepository.deleteAll()
    // Spectrum count
    val spectrumCount: Long = spectrumPersistenceService.count()
    val compoundCount: Long = generateCompoundCount()
    val metaDataValueCount: Long = metaDataRepository.count()
    val metaDataCount: Long = generateMetaDataCount()
    val tagValueCount: Long = tagsRepository.count()
    val tagCount: Long = generateTagCount()
    val submitterCount: Long = generateSubmitterCount()

    // Save global statistics
    globalStatisticsRepository.save(new StatisticsGlobal(new Date, spectrumCount, compoundCount, metaDataCount,
      metaDataValueCount, tagCount, tagValueCount, submitterCount))
    entityManager.flush()
    entityManager.clear()

    "Global Statistics Updated"
  }

  /**
   *
   * @return
   * */
  def getGlobalStatistics: StatisticsGlobal =
    globalStatisticsRepository.findAll()
      .asScala.headOption.getOrElse(new StatisticsGlobal(new Date, 0, 0, 0, 0, 0, 0, 0))


  /**
    * Update all statistics
   * */
  @Async
  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  def updateStatistics(): Unit = {
    metaDataStatisticsService.updateMetaDataStatistics()
    submitterStatisticsService.updateSubmitterStatistics()
    compoundClassStatisticsService.updateCompoundClassStatistics()
    tagStatisticsService.updateTagStatistics()
    updateGlobalStatistics()
    entityManager.flush()
    entityManager.clear()
    logger.info(s"Statistics Update is Completed!")
  }
}
