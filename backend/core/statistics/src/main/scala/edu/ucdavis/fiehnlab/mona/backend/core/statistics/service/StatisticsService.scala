package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.util.Date
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SpectrumRepository, StatisticsGlobalRepository, StatisticsTagRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsGlobal
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.{CompoundRepository, MetaDataRepository, SpectrumSubmitterRepository, TagsRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.{Async, Scheduled}
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

  @Transactional(propagation =  org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def generateCompoundCount(): Long = {
    val inchiKeys: ArrayBuffer[String] = ArrayBuffer()
    compoundRepository.streamAllBy().toScala(Iterator).foreach { compound =>
      compound.getMetaData.asScala.foreach { metadata =>
        if (metadata.getName == "InChIKey") {
          inchiKeys.append(metadata.getValue.substring(0, 14))
        }
      }
    }
    inchiKeys.distinct.length
  }

  @Transactional(propagation =  org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def generateMetaDataCount(): Long = {
    val metaDataCounterMap: Map[String, Int] = Map()
    metaDataRepository.streamAllBy().toScala(Iterator).foreach{ metaData =>
      if(!metaDataCounterMap.contains(metaData.getName)) {
        metaDataCounterMap(metaData.getName) = 1
      }
    }
    metaDataCounterMap.size.toLong
  }

  @Transactional(propagation =  org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def generateTagCount(): Long = {
    val tagsCounter: Map[String, Int] = Map()
    tagsRepository.streamAllBy().toScala(Iterator).foreach { tag =>
      if(tag.getSpectrum == null && tag.getCompound == null) {
        logger.debug(s"Exclude Library Tag Duplicates")
      } else {
        if (!tagsCounter.contains(tag.getText)) {
          tagsCounter(tag.getText) = 1
        }
      }
    }
    tagsCounter.size.toLong
  }

  @Transactional(propagation =  org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def generateSubmitterCount(): Long = {
    val submitterCounter: Map[String, Integer] = Map()
    spectraSubmittersRepository.streamAllBy().toScala(Iterator).foreach { submitter =>
      if (!submitterCounter.contains(submitter.getEmailAddress)) {
        submitterCounter(submitter.getEmailAddress) = 1
      }
    }
    submitterCounter.size.toLong
  }
  /**
    * Update the data in the global statistics repository
    *
    * @return
    **/
  @Transactional(propagation =  org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def updateGlobalStatistics(): StatisticsGlobal = {
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
  }

  /**
   *
   * @return
   * */
  @Transactional(propagation =  org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def getGlobalStatistics: StatisticsGlobal =
    globalStatisticsRepository.findAll()
      .asScala.headOption.getOrElse(new StatisticsGlobal(new Date, 0, 0, 0, 0, 0, 0, 0))


  /**
    * Update all statistics
   * */
  @Async
  @Scheduled(cron = "0 0 0 * * *")
  @Transactional(propagation =  org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def updateStatistics(): Unit = {
    metaDataStatisticsService.updateMetaDataStatistics()
    tagStatisticsService.updateTagStatistics()
    submitterStatisticsService.updateSubmitterStatistics()
    compoundClassStatisticsService.updateCompoundClassStatistics()
    updateGlobalStatistics()
  }
}
