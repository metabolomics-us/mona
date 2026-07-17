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

  // Metadata names whose per value breakdown is charted on the database statistics page
  // (see spectra-database-index.component.ts). Only these names get their values aggregated
  // and stored, since no other consumer reads the value counts of the remaining names
  private val valueDetailNames: List[String] = List("ms level", "ionization mode", "precursor type")

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
    val start = System.currentTimeMillis()
    // Bulk delete child value counts first, then parent rows, in single statements
    statisticsMetaDataRepository.deleteAllMetaDataValueCountsInBatch()
    statisticsMetaDataRepository.deleteAllInBatch()

    // Aggregate the per value breakdown in the database, but only for the charted names, then
    // group the results by name in memory
    val metaDataValueMap: Map[String, ListBuffer[MetaDataValueCount]] = Map()

    metaDataRepository.aggregateValueCountsForNames(valueDetailNames.asJava).asScala.foreach { aggregation =>
      metaDataValueMap.getOrElseUpdate(aggregation.getName, ListBuffer()) +=
        new MetaDataValueCount(aggregation.getValue, aggregation.getCount.toInt)
    }

    // Aggregate the total count for every name in the database and save a parent row per name,
    // attaching the value breakdown only for the charted names
    val nameAggregations = metaDataRepository.aggregateNameCounts().asScala
    nameAggregations.foreach { aggregation =>
      val valueCounts = metaDataValueMap.getOrElse(aggregation.getName, ListBuffer())
      val entry = new StatisticsMetaData(aggregation.getName, aggregation.getCount.toInt, valueCounts.toList.asJava)
      statisticsMetaDataRepository.save(entry)
      entityManager.detach(entry)
    }
    val nameCount = nameAggregations.size
    val valuePairCount = metaDataValueMap.valuesIterator.map(_.size).sum
    metaDataValueMap.clear()
    entityManager.flush()
    entityManager.clear()
    logger.info(f"Metadata statistics complete: $nameCount names, $valuePairCount value pairs across ${valueDetailNames.size} charted names in ${(System.currentTimeMillis() - start) / 1000.0}%.2fs")
    "MetaData Statistics Updated"
  }
}
