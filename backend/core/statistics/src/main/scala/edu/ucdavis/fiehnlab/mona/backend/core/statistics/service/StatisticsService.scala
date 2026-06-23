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
import org.springframework.transaction.annotation.{Propagation, Transactional}

import javax.persistence.EntityManager
import scala.jdk.CollectionConverters._

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


  def generateCompoundCount(): Long = {
    logger.info("Counting unique compounds now...")
    val finalCount = metaDataRepository.countDistinctCompoundInchiKeyBlocks()
    logger.info(s"Counted $finalCount unique compounds")
    finalCount
  }


  def generateMetaDataCount(): Long = {
    logger.info("Counting unique metadata names now...")
    val finalCount = metaDataRepository.countDistinctNames()
    logger.info(s"Counted $finalCount unique metadata names")
    finalCount
  }


  def generateTagCount(): Long = {
    logger.info("Counting unique tags now...")
    val finalCount = tagsRepository.countDistinctTags()
    logger.info(s"Counted $finalCount unique tags")
    finalCount
  }


  def generateSubmitterCount(): Long = {
    logger.info("Counting unique submitters now...")
    val finalCount = spectraSubmittersRepository.countDistinctEmailAddresses()
    logger.info(s"Counted $finalCount unique submitters")
    finalCount
  }
  /**
    * Update the data in the global statistics repository
    *
    * @return
    **/

  def updateGlobalStatistics(): String = {
    logger.info("Updating global statistics now...")
    globalStatisticsRepository.deleteAllInBatch()
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

    logger.info("Global statistics updated")
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
  @Scheduled(cron = "0 0 0 * * *")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  def updateStatistics(): Unit = {
    logger.info("Starting statistics update now...")
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
