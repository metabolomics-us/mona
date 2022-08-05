package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics.{MetaDataValueCount, StatisticsMetaData}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.StatisticsMetaDataRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.MetaDataRepository
import org.springframework.beans.factory.annotation.{Autowired}
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.collection.mutable.{Map}
import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters.StreamHasToScala


/**
  * Created by sajjan on 9/27/16.
  */
@Service
class MetaDataStatisticsService extends LazyLogging{
  @Autowired
  private val statisticsMetaDataRepository: StatisticsMetaDataRepository = null

  @Autowired
  private val metaDataRepository: MetaDataRepository = null

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
 def getMetaDataNames: Array[StatisticsMetaData.StatisticsMetaDataSummary] = {
    statisticsMetaDataRepository.findBy().asScala.toArray
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
  @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def updateMetaDataStatistics(): Unit = {
    statisticsMetaDataRepository.deleteAll()

    val metaDataNameMap: Map[String, Map[String, Int]] = Map()
    val metaDataCounterMap: Map[String, Int] = Map()

    metaDataRepository.streamAllBy().toScala(Iterator).foreach{ metaData =>

      if(metaDataNameMap.contains(metaData.getName)) {
        if(metaDataNameMap(metaData.getName).contains(metaData.getValue)) {
          metaDataNameMap(metaData.getName)(metaData.getValue) += 1
          metaDataCounterMap(metaData.getName)+=1
        } else {
          metaDataNameMap(metaData.getName)(metaData.getValue) = 1
          metaDataCounterMap(metaData.getName) += 1
        }
      } else{
        metaDataNameMap(metaData.getName) = Map(metaData.getValue -> 1)
        metaDataCounterMap(metaData.getName) = 1
      }
    }
    metaDataNameMap.foreach{ case(key, value) =>
      val metaDataValueList: List[MetaDataValueCount] = value.toList.map{case(value, count) =>
        new MetaDataValueCount(value, count)
      }
      statisticsMetaDataRepository.save(new StatisticsMetaData(key, metaDataCounterMap(key), metaDataValueList.asJava))
    }
  }
}
