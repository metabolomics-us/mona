package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
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

  // Guards against overlapping recomputes whether triggered by the admin button or the nightly cron
  private val updateInProgress: AtomicBoolean = new AtomicBoolean(false)

  // True while a statistics recompute is running, used by the admin endpoint to report a conflict
  def isUpdateInProgress: Boolean = updateInProgress.get()


  def generateCompoundCount(): Long = {
    logger.info("Counting unique compounds now...")
    val start = System.currentTimeMillis()
    val finalCount = metaDataRepository.countDistinctCompoundInchiKeyBlocks()
    logger.info(f"Counted $finalCount unique compounds in ${(System.currentTimeMillis() - start) / 1000.0}%.2fs")
    finalCount
  }


  def generateMetaDataCount(): Long = {
    logger.info("Counting unique metadata names now...")
    val start = System.currentTimeMillis()
    val finalCount = metaDataRepository.countDistinctNames()
    logger.info(f"Counted $finalCount unique metadata names in ${(System.currentTimeMillis() - start) / 1000.0}%.2fs")
    finalCount
  }


  def generateTagCount(): Long = {
    logger.info("Counting unique tags now...")
    val start = System.currentTimeMillis()
    val finalCount = tagsRepository.countDistinctTags()
    logger.info(f"Counted $finalCount unique tags in ${(System.currentTimeMillis() - start) / 1000.0}%.2fs")
    finalCount
  }


  def generateSubmitterCount(): Long = {
    logger.info("Counting unique submitters now...")
    val start = System.currentTimeMillis()
    val finalCount = spectraSubmittersRepository.countDistinctEmailAddresses()
    logger.info(f"Counted $finalCount unique submitters in ${(System.currentTimeMillis() - start) / 1000.0}%.2fs")
    finalCount
  }
  /**
    * Update the data in the global statistics repository
    *
    * @return
    **/

  def updateGlobalStatistics(): String = {
    logger.info("Updating global statistics now...")
    val start = System.currentTimeMillis()
    globalStatisticsRepository.deleteAllInBatch()

    // Spectrum count
    var stepStart = System.currentTimeMillis()
    val spectrumCount: Long = spectrumPersistenceService.count()
    logger.info(f"Counted $spectrumCount spectra in ${(System.currentTimeMillis() - stepStart) / 1000.0}%.2fs")

    val compoundCount: Long = generateCompoundCount()

    stepStart = System.currentTimeMillis()
    val metaDataValueCount: Long = metaDataRepository.count()
    logger.info(f"Counted $metaDataValueCount metadata values in ${(System.currentTimeMillis() - stepStart) / 1000.0}%.2fs")

    val metaDataCount: Long = generateMetaDataCount()

    stepStart = System.currentTimeMillis()
    val tagValueCount: Long = tagsRepository.count()
    logger.info(f"Counted $tagValueCount tag values in ${(System.currentTimeMillis() - stepStart) / 1000.0}%.2fs")

    val tagCount: Long = generateTagCount()
    val submitterCount: Long = generateSubmitterCount()

    // Save global statistics
    globalStatisticsRepository.save(new StatisticsGlobal(new Date, spectrumCount, compoundCount, metaDataCount,
      metaDataValueCount, tagCount, tagValueCount, submitterCount))
    entityManager.flush()
    entityManager.clear()

    logger.info(f"Global statistics updated in ${(System.currentTimeMillis() - start) / 1000.0}%.2fs")
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
    // Skip if an update is already running so the admin button and the nightly cron never overlap
    // This is the single shared guard for both trigger paths since both call this method
    if (!updateInProgress.compareAndSet(false, true)) {
      logger.info("Statistics update already in progress, skipping this run")
    } else {
      try {
        logger.info("Starting statistics update now...")
        val start = System.currentTimeMillis()
        metaDataStatisticsService.updateMetaDataStatistics()
        submitterStatisticsService.updateSubmitterStatistics()
        compoundClassStatisticsService.updateCompoundClassStatistics()
        tagStatisticsService.updateTagStatistics()
        updateGlobalStatistics()
        entityManager.flush()
        entityManager.clear()
        logger.info(f"Statistics Update is Completed in ${(System.currentTimeMillis() - start) / 1000.0}%.2fs!")
      } finally {
        updateInProgress.set(false)
      }
    }
  }
}
