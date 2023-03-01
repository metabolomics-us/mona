package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.{MetaDataValueCount, StatisticsMetaData}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{MetaDataRepository, StatisticsMetaDataRepository}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

import scala.collection.mutable.Map
import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters.StreamHasToScala


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

  def updateMetaDataStatisticsHelper(): (Map[String, Map[String, Int]],Map[String, Int]) = {
    val metaDataNameMap: Map[String, Map[String, Int]] = Map()
    val metaDataCounterMap: Map[String, Int] = Map()
    var counter = 0

    metaDataRepository.streamAllBy().toScala(Iterator).foreach { metaData =>

      if (metaDataNameMap.contains(metaData.getName)) {
        if (metaDataNameMap(metaData.getName).contains(metaData.getValue)) {
          metaDataNameMap(metaData.getName)(metaData.getValue) += 1
          metaDataCounterMap(metaData.getName) += 1
        } else {
          metaDataNameMap(metaData.getName)(metaData.getValue) = 1
          metaDataCounterMap(metaData.getName) += 1
        }
      } else {
        metaDataNameMap(metaData.getName) = Map(metaData.getValue -> 1)
        metaDataCounterMap(metaData.getName) = 1
      }
      counter += 1

      if (counter % 100000 == 0) {
        logger.info(s"\tCompleted MetaData Object #${counter}")
        entityManager.flush()
        entityManager.clear()
      }
      entityManager.detach(metaData)
    }
    (metaDataNameMap, metaDataCounterMap)
  }
  /**
    * Update the data in the metadata statistics repository
    *
    * @return
    */
  @Transactional
  def updateMetaDataStatistics(): String = {
    statisticsMetaDataRepository.deleteAll()

    val (metaDataNameMap, metaDataCounterMap) = updateMetaDataStatisticsHelper()
    metaDataNameMap.foreach{ case(key, value) =>
      val metaDataValueList: List[MetaDataValueCount] = value.toList.map{case(value, count) =>
        new MetaDataValueCount(value, count)
      }
      val entry = new StatisticsMetaData(key, metaDataCounterMap(key), metaDataValueList.asJava)
      statisticsMetaDataRepository.save(entry)
      entityManager.detach(entry)

    }

    "MetaData Statistics Updated"
  }
}
