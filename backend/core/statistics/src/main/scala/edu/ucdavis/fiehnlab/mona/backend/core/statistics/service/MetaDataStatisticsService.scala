package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.{MetaDataValueCount, StatisticsMetaData}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{MetaDataRepository, StatisticsMetaDataRepository}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.{Propagation, Transactional}

import javax.persistence.EntityManager
import scala.collection.mutable.{ListBuffer, Map}
import scala.jdk.CollectionConverters._


/**
  * Created by sajjan on 9/27/16.
  */
@Service
@Profile(Array("mona.persistence"))
class MetaDataStatisticsService extends LazyLogging{
  @Autowired
  private val statisticsMetaDataRepository: StatisticsMetaDataRepository = null

  @Autowired
  private val metaDataRepository: MetaDataRepository = null

  @Autowired
  private val entityManager: EntityManager = null

  /**
    * Get all data in the metadata statistics repository
    *
    * @return
    */
  def getMetaDataStatistics: Iterable[StatisticsMetaData] = statisticsMetaDataRepository.findAll().asScala

  /**
    * Get data for the given metadata name from the metadata statistics repository
    *
    * @return
    */
  def getMetaDataStatistics(metaDataName: String): StatisticsMetaData = statisticsMetaDataRepository.findByName(metaDataName)

  /**
    * Get a list of unique metadata names from the metadata statistics repository
    *
    */
 def getMetaDataNames: Array[StatisticsMetaData] = {
    statisticsMetaDataRepository.findByProjection().asScala.toArray
  }

  /**
    * Count the data in the metadata statistics repository
    *
    * @return
    */
  def countMetaDataStatistics: Long = statisticsMetaDataRepository.count()
  /**
    * Update the data in the metadata statistics repository
    *
    * @return
    */
  @Transactional
  def updateMetaDataStatistics(): String = {
    logger.info("Aggregating metadata statistics now...")
    // Bulk delete child value counts first, then parent rows, in single statements
    statisticsMetaDataRepository.deleteAllMetaDataValueCountsInBatch()
    statisticsMetaDataRepository.deleteAllInBatch()

    // Aggregate per name/value counts in the database, then group the results by name in memory
    val metaDataValueMap: Map[String, ListBuffer[MetaDataValueCount]] = Map()
    val metaDataCounterMap: Map[String, Int] = Map()

    metaDataRepository.aggregateValueCounts().asScala.foreach { aggregation =>
      val count = aggregation.getCount.toInt
      metaDataValueMap.getOrElseUpdate(aggregation.getName, ListBuffer()) += new MetaDataValueCount(aggregation.getValue, count)
      metaDataCounterMap(aggregation.getName) = metaDataCounterMap.getOrElse(aggregation.getName, 0) + count
    }

    metaDataValueMap.foreach { case (name, valueCounts) =>
      val entry = new StatisticsMetaData(name, metaDataCounterMap(name), valueCounts.toList.asJava)
      statisticsMetaDataRepository.save(entry)
      entityManager.detach(entry)
    }
    val nameCount = metaDataValueMap.size
    val valuePairCount = metaDataValueMap.valuesIterator.map(_.size).sum
    metaDataValueMap.clear()
    metaDataCounterMap.clear()
    entityManager.flush()
    entityManager.clear()
    logger.info(s"Metadata statistics complete: $nameCount names, $valuePairCount value pairs")
    "MetaData Statistics Updated"
  }
}
